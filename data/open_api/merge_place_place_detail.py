from urllib.parse import urlencode, unquote
import requests, json
import pandas as pd
import db
import api
import re

# db select
sql_select = f"SELECT pd.place_id, pd.rep, pd.info, pd.img_url, pd.intro, pd.story, pd.map_url FROM place_detail as pd"
pds = pd.read_sql(sql_select, con=db.db_connection)
print(len(pds))

"""
for i in range(len(pds)):
    place_id = pds.loc[i, "place_id"]
    rep = pds.loc[i, "rep"]
    info = (
        pds.loc[i, "info"].replace("'", "\\'").replace("%", "%%")
        if pds.loc[i, "info"]
        else pds.loc[i, "info"]
    )
    intro = (
        pds.loc[i, "intro"].replace("'", "\\'").replace("%", "%%")
        if pds.loc[i, "intro"]
        else pds.loc[i, "intro"]
    )
    img_url = pds.loc[i, "img_url"]
    story = (
        pds.loc[i, "story"].replace("'", "\\'").replace("%", "%%")
        if pds.loc[i, "story"]
        else pds.loc[i, "story"]
    )
    map_url = pds.loc[i, "map_url"]

    sql_update = f"UPDATE place SET rep = IF('{rep}' = 'None', null, '{rep}'), info = IF('{info}' = 'None', null, '{info}'), img_url = IF('{img_url}' = 'None', null, '{img_url}') , intro = IF('{intro}' = 'None', null, '{intro}'), story = IF('{story}' = 'None', null, '{story}'), map_url = IF('{map_url}' = 'None', null, '{map_url}') WHERE place_id = {pds.loc[i, 'place_id']}"
    print(sql_update)
    db.conn.execute(sql_update)
"""
