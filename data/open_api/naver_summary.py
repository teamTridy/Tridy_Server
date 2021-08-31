from urllib.parse import urlencode, unquote
import requests, json
import pandas as pd
import db
import api

# db select
sql_select = f"SELECT p.place_id, p.name, pd.story FROM place as p LEFT JOIN place_detail as pd on p.place_id = pd.place_id WHERE pd.story IS NOT NULL"
places = pd.read_sql(sql_select, con=db.db_connection)
print(places)

error_list = []
for i in range(218, len(places)):
    place_id = places.loc[i, "place_id"]
    name = places.loc[i, "name"]
    story = places.loc[i, "story"]

    print(f"<{i}>, place_id: {place_id}")

    # url request
    data = {
        "document": {"title": name, "content": story},
        "option": {"language": "ko", "tone": 1, "summaryCount": 2},
    }
    result = requests.post(
        api.naver_base_url, data=json.dumps(data), headers=api.naver_headers
    )

    if "summary" in result.json():
        summarized_story = (
            result.json()["summary"].replace("'", "\\'").replace("%", "%%")
        )

        # db insert
        sql_update = f"UPDATE place_detail SET story = '{summarized_story}' WHERE place_id = {place_id}"
        print(sql_update)
        db.conn.execute(sql_update)
    else:
        print()
        error_list.append([i, place_id, result.json()])

print(error_list)
