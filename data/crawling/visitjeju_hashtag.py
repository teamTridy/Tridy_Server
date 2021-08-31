from bs4 import BeautifulSoup, element
from selenium import webdriver
import db
import pandas as pd
import sys

sys.stdout = open("stdout.txt", "a", encoding="utf-8")

path = "C:\Programming\Tridy2\data\crawling\chromedriver.exe"
driver = webdriver.Chrome(path)


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


def get_matched_place_id(name):
    sql_select = f"SELECT p.place_id, p.name FROM place as p where REPLACE(p.name,' ', '') like '%%{name}%%'"
    result = pd.read_sql(sql_select, con=db.db_connection)
    print(result)
    if len(result) == 0:
        return None
    else:
        return result.loc[0, "place_id"]


def already_exists_hashtag(place_id):
    sql_select = f"SELECT * FROM place_hashtag WHERE place_id = '{place_id}'"
    result = pd.read_sql(sql_select, con=db.db_connection)
    if len(result) == 0:
        return False
    else:
        print(result)
        return True


category_url = [
    "menuId=DOM_000001718001000000&cate1cd=cate0000000002",
    "menuId=DOM_000001720005000000&cate1cd=cate0000000003",
]  # 관광지, 쇼핑
last_page_mapping = [182, 21]  # 관광지, 쇼핑

try:
    total_cnt = 0
    for cat_idx in range(0, 2):
        if cat_idx == 0:
            start_page = 170
        else:
            start_page = 1
        for i in range(start_page, last_page_mapping[cat_idx] + 1):  # to do
            print(f"######## page: {i} ########")

            list_req = f"https://www.visitjeju.net/kr/detail/list?{category_url[cat_idx]}&isCate1=y&cate2cd=#p{i}&region2cd&pageSize=6&sortListType=reviewcnt&viewType=thumb"  # to do :
            driver.get(list_req)
            html = driver.page_source
            soup = BeautifulSoup(html, "html.parser")
            list = soup.find_all("dl", {"class": "item_section"})

            page_cnt = 0
            for item in list:
                item_detail = item.find("a")
                name = (
                    item_detail.find("p", {"class": "s_tit"})
                    .string.replace(" ", "")
                    .replace("'", "\\'")
                    .replace("%", "%%")
                    .replace('"', '\\"')
                )  # 원활한 검색 위해 공백 제거
                intro = (
                    item_detail.find("p", {"class": "s_theme_tit"})
                    .string.replace("'", "\\'")
                    .replace("%", "%%")
                    .replace('"', '\\"')
                    if item_detail.find("p", {"class": "s_theme_tit"})
                    and item_detail.find("p", {"class": "s_theme_tit"}).string
                    else None
                )

                hashtags = [  # 그리고 db에 넣고 받아와서 id값 연결하는거 필요
                    child.string.replace("#", "")
                    for child in item_detail.find(
                        "p", {"class": "item_tag prev"}
                    ).find_all("a")
                ] + [
                    child.string.replace("#", "")
                    for child in item_detail.find(
                        "p", {"class": "item_tag next"}
                    ).find_all("a")
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
                            address = item.find("p", {"class": "info_sub_cont"}).text

                        elif item.find("p", {"class": "info_sub_tit"}).string == "연락처":
                            rep = (
                                item.find("p", {"class": "info_sub_cont"})
                                .string.replace("(+82)", "")
                                .strip()
                            )

                total_cnt += 1
                page_cnt += 1

                print(f"<{total_cnt}> page:{i}, page_cnt:{page_cnt}")
                print(f"{name}, {address}, {rep}, {intro}")

                # insert hashtag
                print(hashtags)
                hashtag_ids = [get_hashtag_id(hashtag) for hashtag in hashtags]
                place_id = get_matched_place_id(name)

                # if place macthed and 이미 처리되지 않았으면
                if place_id and not already_exists_hashtag(place_id):
                    place_hashtags = [
                        {"place_id": place_id, "hashtag_id": hashtag_id}
                        for hashtag_id in hashtag_ids
                    ]

                    place_hashtag_df = pd.DataFrame(
                        place_hashtags, columns=["place_id", "hashtag_id"]
                    )

                    place_hashtag_df.to_sql(
                        name="place_hashtag",
                        con=db.db_connection,
                        if_exists="append",
                        index=False,
                    )

                    # db update
                    sql_update = f"UPDATE place_detail SET intro = '{intro}' WHERE place_id = {place_id}"
                    print(sql_update)
                    db.conn.execute(sql_update)

                print()
    sys.stdout.close()
except Exception:
    sys.stdout.close()
    raise Exception
