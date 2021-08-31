from urllib.parse import urlencode, unquote
import requests, json
import pandas as pd
import db
import api
import re

# db select
sql_select = f"SELECT pd.place_id, pd.rep, pd.info FROM place_detail as pd WHERE pd.rep = '없음' "
places = pd.read_sql(sql_select, con=db.db_connection)
print(places)

for i in range(len(places)):
    info = places.loc[i, "info"]
    place_id = places.loc[i, "place_id"]
    if re.search("문의 및 안내:(.*)\n", info):
        searched = re.search("문의 및 안내:(.*)\n", info).group()
        rep = re.search("[0-9]+-[0-9]+(-[0-9]+)*", searched).group().strip()

        # db update
        sql_update = (
            f"UPDATE place_detail SET rep = '{rep}' WHERE place_id = {place_id}"
        )
        print(sql_update)
        db.conn.execute(sql_update)
