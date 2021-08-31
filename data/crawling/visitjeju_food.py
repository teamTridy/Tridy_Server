from bs4 import BeautifulSoup, element
from selenium import webdriver
import db
import pandas as pd


def get_location_id(address):
    if "제주시" in address:
        keyword = "제주시"
    elif "서귀포시" in address:
        keyword = "서귀포시"
    else:
        keyword = "제주도"
    sql_select = f"SELECT location_id FROM location WHERE name = '{keyword}'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return result["location_id"][0]


def insert_hashtag(hashtag):
    sql_insert = f"INSERT INTO hashtag(name) VALUES ('{hashtag}')"
    pk = db.conn.execute(sql_insert).lastrowid
    return pk


def get_hashtag_id(hashtag):
    sql_select = f"SELECT hashtag_id FROM hashtag WHERE name = '{hashtag}'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    if len(result) == 0:
        return insert_hashtag(hashtag)
    else:
        return result.loc[0, "hashtag_id"]


def get_last_place_id():
    sql_select = f"SELECT place_id FROM place ORDER BY place_id desc limit 1"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return result.loc[0, "place_id"]


path = "C:\Programming\Tridy2\data\crawling\chromedriver.exe"
driver = webdriver.Chrome(path)

last_page_mapping = {
    65: 19,  # 향토음식
    66: 90,  # 한식
    67: 22,  # 양식
    68: 18,  # 일식
    69: 7,  # 중식
    70: 2,  # 동남아식
    71: 2,  # 할랄프렌들리
    72: 69,  # 카페
}
category_mapping = {
    65: 445,
    66: 445,
    67: 446,
    68: 447,
    69: 448,
    70: 449,
    71: 451,
    72: 453,
}
# 카페, 향토음식 등 카테고리별 crawling
cnt = 0
for cat_idx in range(65, 73):  # 한식~ 할랄프렌들리
    for i in range(1, last_page_mapping[cat_idx] + 1):  # to do
        print(f"######## page: {i} ########")
        list_req = f"https://www.visitjeju.net/kr/detail/list?menuId=DOM_000001719001000000&cate1cd=cate0000000005&isCate1=y&cate2cd=cate00000012{cat_idx}#p{i}&region2cd&pageSize=6&sortListType=reviewcnt&viewType=thumb######"  # to do :
        driver.get(list_req)
        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")
        list = soup.find_all("dl", {"class": "item_section"})
        for item in list:
            item_detail = item.find("a")
            place = {
                "thumb_img_url": item.find("img")["src"],
                "name": item_detail.find("p", {"class": "s_tit"}).string,
                "category_id": category_mapping[cat_idx],  # to do
                "latitude": 0,
                "longitude": 0,
            }

            place_detail = {
                "intro": item_detail.find("p", {"class": "s_theme_tit"}).string,
                "img_url": place["thumb_img_url"],
            }

            hashtags = [  # 그리고 db에 넣고 받아와서 id값 연결하는거 필요
                child.string.replace("#", "")
                for child in item_detail.find("p", {"class": "item_tag prev"}).find_all(
                    "a"
                )
            ] + [
                child.string.replace("#", "")
                for child in item_detail.find("p", {"class": "item_tag next"}).find_all(
                    "a"
                )
            ]

            detail_req = "https://www.visitjeju.net" + item_detail["href"]
            driver.implicitly_wait(2)
            driver.get(detail_req)
            html = driver.page_source
            soup = BeautifulSoup(html, "html.parser")

            # 주소,연락처등 기본 정보
            for item in soup.find("div", {"class": "basic_information"}).find_all(
                "div", {"class": "clear"}
            ):
                if item.p:
                    if item.find("p", {"class": "info_sub_tit"}).string == "주소":
                        place["address"] = item.find(
                            "p", {"class": "info_sub_cont"}
                        ).text
                        place["location_id"] = get_location_id(place["address"])
                    elif item.find("p", {"class": "info_sub_tit"}).string == "연락처":
                        place_detail["rep"] = (
                            item.find("p", {"class": "info_sub_cont"})
                            .string.replace("(+82)", "")
                            .strip()
                        )

            # story
            story = ""
            if soup.find("div", {"class": "real"}):
                for item in soup.find("div", {"class": "real"}).find_all("p"):
                    story += item.text
            place_detail["story"] = story[0:1023] if len(story) > 0 else None

            # 소개, 이용정보
            info = ""
            for item in (
                soup.find("div", {"class": "add2020_detail_side_info"})
                .find("dl")
                .children
            ):

                if type(item) == element.Tag:
                    if item.name == "dt" and item.text != "소개":
                        info += item.text + ": "
                    if item.name == "dd" and item.text != place_detail["intro"]:
                        cleansed_text = " ".join(
                            [text for text in item.text.split()]
                        )  # 긴 공백 제거
                        info += cleansed_text + "\n"

                place_detail["info"] = info
            cnt += 1

            print(f"<{cnt}>")
            print(place)
            print(place_detail)
            print(hashtags)

            place_columns = [
                "thumb_img_url",
                "name",
                "category_id",
                "latitude",
                "longitude",
                "address",
                "location_id",
            ]

            place_df = pd.DataFrame(
                place, columns=place_columns, index=[0]
            )  # ictionary를 pandas dataframe으로 만들 때 종종 나는 error.

            place_df.to_sql(
                name="place", con=db.db_connection, if_exists="append", index=False
            )

            place_detail_columns = [
                "intro",
                "img_url",
                "rep",
                "story",
                "info",
                "place_id",
            ]
            place_detail["place_id"] = get_last_place_id()
            place_detail_df = pd.DataFrame(
                place_detail, columns=place_detail_columns, index=[0]
            )
            place_detail_df.to_sql(
                name="place_detail",
                con=db.db_connection,
                if_exists="append",
                index=False,
            )

            place_hashtags = []
            for hashtag in hashtags:
                place_hashtags.append(
                    {
                        "place_id": place_detail["place_id"],
                        "hashtag_id": get_hashtag_id(hashtag),
                    }
                )

            place_hashtag_df = pd.DataFrame(
                place_hashtags, columns=["place_id", "hashtag_id"]
            )

            place_hashtag_df.to_sql(
                name="place_hashtag",
                con=db.db_connection,
                if_exists="append",
                index=False,
            )
