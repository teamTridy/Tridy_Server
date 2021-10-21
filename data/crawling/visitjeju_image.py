from bs4 import BeautifulSoup
from selenium import webdriver
import db
import pandas as pd
import sys

sys.stdout = open("stdout2.txt", "w", encoding="utf-8")

path = "C:\Programming\Tridy_Server\data\crawling\chromedriver.exe"
driver = webdriver.Chrome(path)


def get_matched_place_id(name):
    sql_select = f"SELECT p.place_id, p.name FROM place as p where REPLACE(p.name,' ', '') like '%%{name}%%' and thumb_img_url is null"
    result = pd.read_sql(sql_select, con=db.db_connection)
    print(result)
    if len(result) == 0:
        return None
    else:
        return result.loc[0, "place_id"]


category_url = [
    "menuId=DOM_000001718001000000&cate1cd=cate0000000002",
    "menuId=DOM_000001720005000000&cate1cd=cate0000000003",
]  # 관광지, 쇼핑
last_page_mapping = [182, 21]  # 관광지, 쇼핑

try:
    total_cnt = 0
    for cat_idx in range(0, 2):
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
                thumb_img_url = item.find("img")["src"]
                name = (
                    item.find("a")
                    .find("p", {"class": "s_tit"})
                    .string.replace(" ", "")
                    .replace("'", "\\'")
                    .replace("%", "%%")
                    .replace('"', '\\"')
                )  # 원활한 검색 위해 공백 제거
                total_cnt += 1
                page_cnt += 1

                print(f"<{total_cnt}> page:{i}, page_cnt:{page_cnt}")
                print(f"{name}, {thumb_img_url}")

                place_id = get_matched_place_id(name)

                # if place macthed and 이미 처리되지 않았으면
                if place_id:
                    # db update
                    sql_update = f"UPDATE place SET thumb_img_url = '{thumb_img_url}', img_url = '{thumb_img_url}' WHERE place_id = {place_id}"
                    print(sql_update)
                    db.conn.execute(sql_update)

                print()
    sys.stdout.close()
except Exception:
    sys.stdout.close()
    raise Exception
