# -*- coding: utf-8 -*-

from urllib.parse import urlencode, unquote
import json
import requests
from bs4 import BeautifulSoup
import pandas as pd
import db
import api
import datetime
import re
import slack as Slack


def convert_to_formmatted_datetime(str_date):
    year = int(str_date[0:4])
    month = int(str_date[4:6])
    day = int(str_date[6:8])
    return datetime.datetime(year, month, day)


def convert_to_formmatted_str_date(str_date):
    year = int(str_date[0:4])
    month = int(str_date[4:6])
    day = int(str_date[6:8])
    return str(datetime.date(year, month, day))


def get_category_id(origin_code):
    sql_select = f"SELECT category_id FROM category WHERE origin_code = '{origin_code}'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return result["category_id"][0]


def get_region_id(address):
    if "제주시" in address:
        keyword = "제주시"
    elif "서귀포시" in address:
        keyword = "서귀포시"
    else:
        keyword = "제주도"
    sql_select = f"SELECT region_id FROM region WHERE name = '{keyword}'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return result["region_id"][0]


def already_exists_hashtag(place_id):
    sql_select = f"SELECT * FROM place_hashtag WHERE place_id = '{place_id}'"
    result = pd.read_sql(sql_select, con=db.db_connection)
    if len(result) == 0:
        return False
    else:
        return True


def insert_place_hashtag_by_category(place_id, category_id):
    if place_id and not already_exists_hashtag(place_id):
        hashtag_ids = api.category_hashtag_matching[category_id]

        place_hashtags = [
            {
                "place_id": place_id,
                "hashtag_id": hashtag_id,
                "created_at": convert_to_formmatted_datetime(str_yesterday),
                "updated_at": convert_to_formmatted_datetime(str_yesterday),
            }
            for hashtag_id in hashtag_ids
        ]

        place_hashtag_df = pd.DataFrame(
            place_hashtags,
            columns=["place_id", "hashtag_id", "created_at", "updated_at"],
        )

        json_object = json.loads(place_hashtag_df.to_json(orient="records"))
        json_formatted_str = json.dumps(json_object, indent=2, ensure_ascii=False)
        slack.concat_message(json_formatted_str)

        place_hashtag_df.to_sql(
            name="place_hashtag",
            con=db.db_connection,
            if_exists="append",
            index=False,
        )


def get_place_id_by_origin_content_id(origin_content_id):
    sql_select = (
        f"SELECT place_id FROM place WHERE origin_content_id = {origin_content_id}"
    )
    result = pd.read_sql(sql_select, con=db.db_connection)
    return result["place_id"][0]


# db setting
rows = list()
columns = [
    "address",
    "latitude",
    "longitude",
    "name",
    "origin_content_id",
    "img_url",
    "thumb_img_url",
    "category_id",
    "region_id",
    "origin_content_type_id",
    "created_at",
    "updated_at",
    "rep",
    "intro",
]


today = datetime.datetime.now()
yesterday = today - datetime.timedelta(1)
str_yesterday = yesterday.strftime("%Y%m%d")

# 슬랙 생성
slack = Slack.generate("insert_update")
slack.concat_message(
    f"===================== {str_yesterday} 추가/수정 결과 =====================\n\n\n\n"
)

try:
    # url setting
    area_based_list_base_url = api.tour_base_url + "/areaBasedList?"
    request_url = area_based_list_base_url + urlencode(
        {
            "ServiceKey": unquote(api.tour_service_key2),
            "MobileOS": "ETC",
            "MobileApp": "Tridy",
            "numOfRows": 5000,
            "areaCode": 39,  # 제주도
            "arrange": "B",
            "modifiedtime": str_yesterday,  # str_yesterday,
        }
    )

    detail_common_base_url = (  # 공통정보조회
        api.tour_base_url
        + "/detailCommon?"
        + urlencode(
            {
                "ServiceKey": unquote(api.tour_service_key2),
                "MobileOS": "ETC",
                "MobileApp": "Tridy",
                "firstImageYN": "Y",
                "overviewYN": "Y",
                "defaultYN": "Y",
            }
        )
    )

    detail_intro_base_url = (  # 소개정보조회
        api.tour_base_url
        + "/detailIntro?"
        + urlencode(
            {
                "ServiceKey": unquote(api.tour_service_key2),
                "MobileOS": "ETC",
                "MobileApp": "Tridy",
            }
        )
    )

    image_base_url = (  # 이미지정보조회
        api.tour_base_url
        + "/detailImage?"
        + urlencode(
            {
                "ServiceKey": unquote(api.tour_service_key2),
                "MobileOS": "ETC",
                "MobileApp": "Tridy",
                "_type": "json",
            }
        )
    )

    # url request
    places = list()
    response = requests.get(request_url)
    soup = BeautifulSoup(response.content, "html.parser")
    for content in soup.find_all("item"):
        if (
            "B" not in content.cat3.string
            and "C" not in content.cat3.string
            and "A05" not in content.cat3.string
        ):  # 숙박업소, 코스, 음식점 제거
            address = (
                content.addr1.string if content.addr1 else "제주도"
            )  # 주소값이 없는 컨텐츠면 제주도로 입력
            places.append(
                {
                    "address": address,
                    "longitude": content.mapx.string if content.mapx else 0,
                    "latitude": content.mapy.string if content.mapy else 0,
                    "name": content.title.string,
                    "origin_content_id": content.contentid.string,
                    "img_url": content.firstimage.string
                    if content.firstimage
                    else None,
                    "thumb_img_url": content.firstimage2.string
                    if content.firstimage2
                    else None,
                    "category_id": get_category_id(content.cat3.string),  # 함수
                    "region_id": get_region_id(address),  # 함수
                    "origin_content_type_id": content.contenttypeid.string,
                    "created_at": convert_to_formmatted_datetime(
                        content.createdtime.string
                    ),
                    "updated_at": convert_to_formmatted_datetime(
                        content.modifiedtime.string
                    ),
                    "rep": content.tel.string if content.tel else "--",
                }
            )

    for idx in range(len(places)):
        slack.concat_message(f"\n\n----------[{idx}]------------")
        sql_select = f"SELECT story FROM place WHERE origin_content_id = {places[idx]['origin_content_id']}"
        db_place = pd.read_sql_query(sql_select, db.db_connection)

        if len(db_place) != 0:
            is_new_content = False
            sql_update = f"UPDATE place SET address = '{places[idx]['address']}', longitude = {places[idx]['longitude']}, latitude = {places[idx]['latitude']}, name = '{places[idx]['name']}', img_url = IF('{places[idx]['img_url']}' != 'None', '{places[idx]['img_url']}', null), thumb_img_url = IF('{places[idx]['thumb_img_url']}' != 'None', '{places[idx]['thumb_img_url']}', null), category_id = {places[idx]['category_id']}, region_id = {places[idx]['region_id']}, origin_content_type_id = {places[idx]['origin_content_type_id']}, created_at = '{places[idx]['created_at']}', updated_at = '{places[idx]['updated_at']}', rep = '{places[idx]['rep']}' WHERE origin_content_id = {places[idx]['origin_content_id']}"
            slack.concat_message(sql_update)
            db.conn.execute(sql_update)

        else:
            is_new_content = True
            places[idx]["intro"] = "트리디가 이 여행지의 매력을 곧 소개해드릴게요"
            content_df = pd.DataFrame(places[idx], index=[0], columns=columns)

            json_object = json.loads(content_df.iloc[0].to_json())
            json_formatted_str = json.dumps(json_object, indent=2, ensure_ascii=False)
            slack.concat_message(json_formatted_str)

            content_df.to_sql(
                name="place", con=db.db_connection, if_exists="append", index=False
            )

        # insert_new_hashtag
        insert_place_hashtag_by_category(
            get_place_id_by_origin_content_id(places[idx]["origin_content_id"]),
            places[idx]["category_id"],
        )

        ############# get overview
        detail_common_request_url = (
            detail_common_base_url
            + "&"
            + urlencode({"contentId": places[idx]["origin_content_id"]})
        )
        detail_common_response = requests.get(detail_common_request_url)
        detail_common_soup = BeautifulSoup(
            detail_common_response.content, "html.parser"
        )

        covid_info = ""
        for item in detail_common_soup.find_all("item"):
            story = (
                item.overview.string[:1023]
                .replace("<br />", "")
                .replace("<br>", "")
                .replace("<br/>", "")
                .replace("nbsp;", "")
                .replace("%", "%%")
                if item.overview
                and item.overview.string
                and len(item.overview.string) > 0
                else None
            )

            if story:
                slack.concat_message("origin_story=" + story)

            # 코로나 감염증 관련 정보 추출
            if story is not None and re.search("<b>(.*)</b>", story):
                covid_info = re.search("<b>(.*)</b>", story).group().strip()
                covid_info = re.sub(re.compile("<.*?>"), "", covid_info)

            # 새 데이터일경우에만 네이버 요약 함수 실행
            if story is not None and (
                is_new_content or db_place.loc[0]["story"] is None
            ):
                # 정제
                story = re.sub(
                    re.compile("<b>※(.*)</u>"), "", story
                )  # 코로나 감염증 관련 정보 제거
                story = re.sub(re.compile("<.*?>"), "", story)

                try:
                    # url request
                    data = {
                        "document": {
                            "title": places[idx]["name"],
                            "content": story,
                        },
                        "option": {"language": "ko", "tone": 1, "summaryCount": 2},
                    }
                    result = requests.post(
                        api.naver_base_url,
                        data=json.dumps(data),
                        headers=api.naver_headers,
                    )

                    if "summary" in result.json():
                        summarized_story = (
                            result.json()["summary"]
                            .replace("'", "\\'")
                            .replace("%", "%%")
                        )

                        slack.concat_message("summerazied_story=" + summarized_story)
                        # db insert
                        sql_update = f"UPDATE place SET story = '{summarized_story}' WHERE origin_content_id = {places[idx]['origin_content_id']}"
                        db.conn.execute(sql_update)
                        slack.concat_message(sql_update)

                except Exception as e:
                    slack.concat_message(
                        (
                            f"\n\n\U0001F62D 네이버 요약 오류 발생:\n {e} \n\n\n\n"
                            "============================================================="
                        )
                    )

        ############# get info
        detail_intro_request_url = (
            detail_intro_base_url
            + "&"
            + urlencode(
                {
                    "contentId": places[idx]["origin_content_id"],
                    "contentTypeId": places[idx]["origin_content_type_id"],
                }
            )
        )
        detail_intro_response = requests.get(detail_intro_request_url)
        detail_intro_soup = BeautifulSoup(detail_intro_response.content, "html.parser")

        # 우선순위 3인 항목 제거
        filtered_childrens = list(
            filter(
                lambda x: api.tour_detail_intro_keywords_with_priority[x.name][1] != 3,
                detail_intro_soup.find("item").children,  # iterable
            )
        )
        # 우선순위대로 정렬
        sorted_childrens = sorted(
            filtered_childrens,
            key=lambda x: api.tour_detail_intro_keywords_with_priority[x.name][1],
        )

        info = covid_info + "\n" if covid_info != "" else ""  # 코로나 관련 정보를 맨 앞으로
        for children in sorted_childrens:
            if children and children.string:  # none이 아닌 경우에만
                title = api.tour_detail_intro_keywords_with_priority[children.name][0]

                if title == "행사 시작일" or title == "행사 종료일":
                    content = convert_to_formmatted_str_date(children.string)
                elif "여부" in title or "유무" in title:  # 여부,유무 값 1,0 => O,X 변환
                    content = children.string.replace("0", "X").replace("1", "O")
                else:  # <br /> => \n변환
                    content = (
                        children.string.replace("<br />", "")
                        .replace("<br/>", "")
                        .replace("<br>", "")
                        .replace("<br />", "")
                        .replace("nbsp;", "")
                        .replace("'", "\\'")
                        .replace('"', '\\"')
                        .replace("%", "%%")
                    )

                info += title + ": " + content + "\n"

        # 정제
        info = re.sub(re.compile("<.*?>"), "", info)
        sql_update = f"UPDATE place SET info = '{info}' WHERE origin_content_id = {places[idx]['origin_content_id']}"
        slack.concat_message(sql_update)
        db.conn.execute(sql_update)

        ############## update rep
        if (
            places[idx]["rep"] == "--"
            and re.search("문의 및 안내:(.*)\n", info)
            and re.search(
                "[0-9]+-[0-9]+(-[0-9]+)*", re.search("문의 및 안내:(.*)\n", info).group()
            )
        ):
            searched = re.search("문의 및 안내:(.*)\n", info).group()
            rep = re.search("[0-9]+-[0-9]+(-[0-9]+)*", searched).group().strip()
            sql_update = f"UPDATE place SET rep = '{rep}' WHERE origin_content_id = {places[idx]['origin_content_id']} AND rep = '--'"
            db.conn.execute(sql_update)

        ############# update img_url
        if places[idx]["img_url"] is None:
            tour_image_request_url = (
                image_base_url
                + "&"
                + urlencode({"contentId": places[idx]["origin_content_id"]})
            )
            result = requests.get(tour_image_request_url).json()

            if "item" in result["response"]["body"]["items"]:
                if type(result["response"]["body"]["items"]["item"]) == list:
                    item = result["response"]["body"]["items"]["item"][0]
                    img_url = item["originimgurl"]
                    thumb_img_url = item["smallimageurl"]

                elif type(result["response"]["body"]["items"]["item"]) == dict:
                    item = result["response"]["body"]["items"]["item"]
                    img_url = item["originimgurl"]
                    thumb_img_url = item["smallimageurl"]

                sql_update = f"UPDATE place SET img_url = '{img_url}', thumb_img_url = '{thumb_img_url}' WHERE origin_content_id = {places[idx]['origin_content_id']}"
                slack.concat_message(sql_update)
                db.conn.execute(sql_update)

    slack.concat_message(
        (
            f"\n\n\u2705 갱신 성공 \n\n\n\n"
            "============================================================="
        )
    )
    slack.send_message_to_slack()
except Exception as e:
    slack.concat_message(
        (
            f"\n\n\U0001F62D 오류 발생:\n {e} \n\n\n\n"
            "============================================================="
        )
    )
    slack.send_message_to_slack()
    raise e