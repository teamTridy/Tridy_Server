from urllib.parse import urlencode, unquote
import requests
from bs4 import BeautifulSoup
import pandas as pd
import db
import api
import datetime


def convert_to_formmatted_datetime(str_date):
    year = int(str_date[0:4])
    month = int(str_date[4:6])
    day = int(str_date[6:8])
    return datetime.datetime(year, month, day)


def get_category_id(origin_code):
    sql_select = f"SELECT category_id FROM category WHERE origin_code = '{origin_code}'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return result["category_id"][0]


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


# db setting
rows = list()
columns = [
    "address",
    "latitude",
    "longitude",
    "name",
    "origin_content_id",
    "thumb_img_url",
    "category_id",
    "location_id",
    "origin_content_type_id",
    "created_at",
    "updated_at",
]

# url setting
area_based_list_base_url = api.tour_base_url + "/areaBasedList?"
request_url = area_based_list_base_url + urlencode(
    {
        "ServiceKey": unquote(api.tour_service_key),
        "MobileOS": "ETC",
        "MobileApp": "Tridy",
        "numOfRows": 5000,
        "areaCode": 39,  # 제주도
        "arrange": "B",
    }
)

# url request
response = requests.get(request_url)
soup = BeautifulSoup(response.content, "html.parser")
for content in soup.find_all("item"):
    if "B" not in content.cat3.string or "C" not in content.cat3.string:  # 숙박업소, 코스 제거
        address = (
            content.addr1.string if content.addr1 else "제주도"
        )  # 주소값이 없는 컨텐츠면 제주도로 입력
        rows.append(
            {
                "address": address,
                "latitude": content.mapx.string if content.mapx else 0,
                "longitude": content.mapy.string if content.mapx else 0,
                "name": content.title.string,
                "origin_content_id": content.contentid.string,
                "thumb_img_url": content.firstimage2.string
                if content.firstimage2
                else None,
                "category_id": get_category_id(content.cat3.string),  # 함수
                "location_id": get_location_id(address),  # 함수
                "origin_content_type_id": content.contenttypeid.string,
                "created_at": convert_to_formmatted_datetime(
                    content.createdtime.string
                ),
                "updated_at": convert_to_formmatted_datetime(
                    content.modifiedtime.string
                ),
            }
        )

# db insert
content_df = pd.DataFrame(rows, columns=columns)
content_df.to_sql(name="place", con=db.db_connection, if_exists="append", index=False)