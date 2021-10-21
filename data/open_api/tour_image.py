from urllib.parse import urlencode, unquote
import requests
import pandas as pd
import db
import api

# url setting
image_base_url = (  # 공통정보조회
    api.tour_base_url
    + "/detailImage?"
    + urlencode(
        {
            "ServiceKey": unquote(api.tour_service_key3),
            "MobileOS": "ETC",
            "MobileApp": "Tridy",
            "_type": "json",
        }
    )
)


# db select
sql_select = f"SELECT * FROM tridy.place where thumb_img_url is null and origin_content_id is not null;"
places = pd.read_sql(sql_select, con=db.db_connection)
for i in range(len(places)):
    place_id = places.loc[i, "place_id"]
    origin_content_id = places.loc[i, "origin_content_id"]

    # url request
    tour_image_request_url = (
        image_base_url + "&" + urlencode({"contentId": origin_content_id})
    )
    result = requests.get(tour_image_request_url).json()

    print(place_id, origin_content_id)
    if "item" in result["response"]["body"]["items"]:
        if type(result["response"]["body"]["items"]["item"]) == list:
            item = result["response"]["body"]["items"]["item"][0]
            img_url = item["originimgurl"]
            thumb_img_url = item["smallimageurl"]

        elif type(result["response"]["body"]["items"]["item"]) == dict:
            item = result["response"]["body"]["items"]["item"]
            img_url = item["originimgurl"]
            thumb_img_url = item["smallimageurl"]

        sql_update = f"UPDATE place SET img_url = '{img_url}', thumb_img_url = '{thumb_img_url}' WHERE place_id = {place_id}"
        print(sql_update)
        db.conn.execute(sql_update)