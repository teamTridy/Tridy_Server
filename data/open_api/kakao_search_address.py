from urllib.parse import urlencode, unquote
import requests
import pandas as pd
import db
import api

# db select
sql_select = f"SELECT place_id, address FROM place WHERE latitude = 0"  # lat,lng없는 비짓제주 데이터들 가져오기
places = pd.read_sql(sql_select, con=db.db_connection)

for i in range(len(places)):
    place_id = places.loc[i, "place_id"]
    address = places.loc[i, "address"]
    print(places.loc[i, :])

    # url request
    kakao_search_adderss_request_url = (
        api.kakao_base_url + "/search/address.json?" + urlencode({"query": address})
    )

    result = requests.post(
        kakao_search_adderss_request_url, headers=api.kakao_headers
    ).json()

    if len(result["documents"]) > 0:  # 검색 결과가 존재할때만 insert
        lat = result["documents"][0]["x"]
        lng = result["documents"][0]["y"]

        # db insert
        sql_update = f"UPDATE place SET latitude = {lat}, longitude = {lng} WHERE place_id = {place_id}"
        print(sql_update)
        db.conn.execute(sql_update)
