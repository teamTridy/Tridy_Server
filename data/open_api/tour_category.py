from urllib.parse import urlencode, unquote
import requests
from bs4 import BeautifulSoup
import pandas as pd
import db
import api


def get_request_url_by_category_level(base_url, category_level, category_code):
    request_url = str()

    if category_level == 1:  # 대분류
        request_url = base_url + urlencode(
            {
                "ServiceKey": unquote(api.tour_service_key),
                "MobileOS": "ETC",
                "MobileApp": "Tridy",
                "numOfRows": 50,
            }
        )

    elif category_level == 2:  # 중분류
        request_url = base_url + "&" + urlencode({"cat1": category_code})

    elif category_level == 3:  # 소분류
        request_url = base_url + "&" + urlencode({"cat2": category_code})

    return request_url


def get_category(base_url, category_level, category_code):
    if category_level > 3:
        return

    request_url = get_request_url_by_category_level(
        base_url, category_level, category_code
    )
    response = requests.get(request_url)
    soup = BeautifulSoup(
        response.content, "html.parser"
    )  # content 속성을 통해 바이너리 원문, text 속성을 통해 UTF-8로 인코딩된 문자열

    for category in soup.find_all("item"):
        get_category(request_url, category_level + 1, category.code.string)
        if (
            category_level == 3
            and "B" not in category.find("name")
            and "C" not in category.find("name")
        ):  # 소분류만 디비에 저장
            rows.append(
                {
                    "depth": 3,
                    "name": category.find("name").string,
                    "origin_code": category.code.string,
                }
            )


# db setting
rows = list()
columns = ["depth", "name", "origin_code"]

# url setting
category_base_url = api.tour_base_url + "/categoryCode?"

# url request
get_category(category_base_url, 1, None)

# db insert
category_df = pd.DataFrame(rows, columns=columns)
category_df.to_sql(
    name="category", con=db.db_connection, if_exists="append", index=False
)
