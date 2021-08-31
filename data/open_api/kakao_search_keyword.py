from urllib.parse import urlencode, unquote
import requests
import pandas as pd
import db
import api
import re


def get_cleansed_name(name):
    regex = "\(.*\)|\s-\s.*|\[.*\]"
    cleansed_name = re.sub(regex, "", name)
    regex2 = "2019|2020|2021|2022"
    cleansed_name = re.sub(regex2, "", cleansed_name)
    regex3 = '".*"'
    cleansed_name = (
        re.search(regex3, cleansed_name).group()
        if re.search(regex3, cleansed_name)
        else cleansed_name
    )
    return cleansed_name


# db select
sql_select = f"SELECT place_id, name, latitude, longitude FROM place"
places = pd.read_sql(sql_select, con=db.db_connection)
for i in range(len(places)):
    place_id = places.loc[i, "place_id"]
    name = places.loc[i, "name"]
    lat = places.loc[i, "latitude"]
    lng = places.loc[i, "longitude"]
    print(place_id)

    # url request
    kakao_search_keyword_request_url = (
        api.kakao_base_url
        + "/search/keyword.json?"
        + urlencode({"query": get_cleansed_name(name), "x": lat, "y": lng})
    )

    print(get_cleansed_name(name), lat, lng)

    result = requests.post(
        kakao_search_keyword_request_url, headers=api.kakao_headers
    ).json()

    if len(result["documents"]) > 0:  # 검색 결과가 존재할때만 insert
        place_url = result["documents"][0]["place_url"]
        phone = result["documents"][0]["phone"]

        if len(phone) > 0:
            sql_update = f"UPDATE place_detail SET map_url = '{place_url}', rep = IF(rep IN ('없음', '--'), '{phone}', rep) WHERE place_id = {place_id}"  # phone번호가 api로 나오고 db에는 없을 때
        else:
            sql_update = f"UPDATE place_detail SET map_url = '{place_url}' WHERE place_id = {place_id}"  # phone번호가 api로 나오고 db에는 없을 때
        print(sql_update)
        db.conn.execute(sql_update)
