from urllib.parse import urlencode, unquote
import requests
from bs4 import BeautifulSoup
import pandas as pd
import db
import api
import datetime


def convert_to_formmatted_str_date(str_date):
    year = int(str_date[0:4])
    month = int(str_date[4:6])
    day = int(str_date[6:8])
    return str(datetime.date(year, month, day))


# url setting
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

# db select
sql_select = f"SELECT place_id, origin_content_id, origin_content_type_id FROM place"
places = pd.read_sql_query(sql_select, db.db_connection)

# url request
for i in range(len(places)):
    rows = list()

    place_id = places.loc[i, "place_id"]
    origin_content_id = places.loc[i, "origin_content_id"]
    origin_content_type_id = places.loc[i, "origin_content_type_id"]
    print(places.loc[i, :])

    # detail_common
    detail_common_request_url = (
        detail_common_base_url + "&" + urlencode({"contentId": origin_content_id})
    )

    detail_common_response = requests.get(detail_common_request_url)
    detail_common_soup = BeautifulSoup(detail_common_response.content, "html.parser")

    for item in detail_common_soup.find_all("item"):

        story = (
            item.overview.string[:1023].replace("<br />", "").replace("<br>", "")
            if item.overview and item.overview.string
            else "트리디가 이 여행지의 매력을 곧 소개해드릴게요"
        )
        rep = item.tel.string if item.tel and item.tel.string else "없음"
        img_url = item.firstimage.string if item.firstimage else None

    # detail_intro
    detail_intro_request_url = (
        detail_intro_base_url
        + "&"
        + urlencode(
            {"contentId": origin_content_id, "contentTypeId": origin_content_type_id}
        )
    )
    detail_intro_response = requests.get(detail_intro_request_url)
    detail_intro_soup = BeautifulSoup(detail_intro_response.content, "html.parser")

    info = ""

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

    for children in sorted_childrens:
        if children and children.string:  # none이 아닌 경우에만
            title = api.tour_detail_intro_keywords_with_priority[children.name][0]

            if title == "행사 시작일" or title == "행사 종료일":
                content = convert_to_formmatted_str_date(children.string)
            elif "여부" in title or "유무" in title:  # 여부,유무 값 1,0 => O,X 변환
                content = children.string.replace("0", "X").replace("1", "O")
            else:  # <br /> => \n변환
                content = children.string.replace("<br />", "")

            info += title + ": " + content + "\n"

    rows.append(
        {
            "place_id": place_id,
            "story": story,
            "img_url": img_url,
            "rep": rep,
            "info": info,
        }
    )

    # 원래 다 모아놓고 한번에 insert하려고 했으나 자꾸 멈춰서 그냥 즉시 넣도록함.
    content_detil_df = pd.DataFrame(
        rows, columns=["place_id", "story", "img_url", "rep", "info"]
    )
    content_detil_df.to_sql(
        name="place_detail", con=db.db_connection, if_exists="append", index=False
    )
