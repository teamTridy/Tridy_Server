# -*- coding: utf-8 -*-

from urllib.parse import urlencode, unquote
import json
import requests
from bs4 import BeautifulSoup
import pandas as pd
import db
import api
import datetime
import re
import slack

# 슬랙 생성
today = datetime.datetime.now()
yesterday = today - datetime.timedelta(1)
str_today = today.strftime("%Y%m%d")
str_yesterday = yesterday.strftime("%Y%m%d")

slack = slack.generate("delete")
slack.concat_message(
    f"===================== {str_yesterday} 삭제 결과 =====================\n\n\n\n"
)

try:
    # url setting
    area_based_list_base_url = api.tour_base_url + "/areaBasedList?"
    area_based_list_request_url = area_based_list_base_url + urlencode(
        {
            "ServiceKey": unquote(api.tour_service_key2),
            "MobileOS": "ETC",
            "MobileApp": "Tridy",
            "numOfRows": 5000,
            "areaCode": 39,  # 제주도
            "arrange": "B",
        }
    )

    festival_search_base_url = api.tour_base_url + "/searchFestival?"
    festival_search_request_url = festival_search_base_url + urlencode(
        {
            "ServiceKey": unquote(api.tour_service_key2),
            "MobileOS": "ETC",
            "MobileApp": "Tridy",
            "numOfRows": 5000,
            "areaCode": 39,  # 제주도
            "arrange": "B",
            "eventStartDate": str_yesterday,
            "eventEndDate": str_yesterday,
        }
    )

    # url request
    deleted_origin_content_id_list = list()

    ######### 전제적으로 검사
    places = list()
    response = requests.get(area_based_list_request_url)
    soup = BeautifulSoup(response.content, "html.parser")
    api_all_origin_content_id_list = [
        int(content.contentid.string) for content in soup.find_all("item")
    ]

    sql_select = (
        f"SELECT origin_content_id FROM place WHERE origin_content_id is not null"
    )
    places = pd.read_sql_query(sql_select, db.db_connection)
    db_all_origin_content_id_list = places["origin_content_id"].values.tolist()

    deleted_all_origin_content_id_list = list(
        set(db_all_origin_content_id_list) - set(api_all_origin_content_id_list)
    )

    slack.concat_message(
        f"\n\n----------전체 검수 - 총 [{len(deleted_all_origin_content_id_list)}]개 삭제 시도------------"
    )

    for deleted_origin_content_id in deleted_all_origin_content_id_list:
        sql_delete = f"DELETE FROM place WHERE origin_content_id = {deleted_origin_content_id} AND NOT (select count(*) from recommend where place_id = (select pid from (select place_id as pid from place  where origin_content_id = {deleted_origin_content_id}) as a) and created_at>'{str_today}');"
        slack.concat_message(sql_delete)
        db.conn.execute(sql_delete)

    #### 행사만 검사
    places = list()
    response = requests.get(festival_search_request_url)
    soup = BeautifulSoup(response.content, "html.parser")
    api_going_on_festival_origin_content_id_list = [
        int(content.contentid.string) for content in soup.find_all("item")
    ]

    sql_select = f"SELECT origin_content_id FROM place WHERE origin_content_id is not null AND origin_content_type_id = 15"
    places = pd.read_sql_query(sql_select, db.db_connection)
    db_festival_origin_content_id_list = places["origin_content_id"].values.tolist()

    closed_festival_origin_content_id_list = list(
        set(db_festival_origin_content_id_list)
        - set(api_going_on_festival_origin_content_id_list)
    )

    slack.concat_message(
        f"\n\n----------행사 검수 - 총 [{len(closed_festival_origin_content_id_list)}]개 삭제 시도------------"
    )

    for closed_origin_content_id in closed_festival_origin_content_id_list:
        sql_delete = f"DELETE FROM place WHERE origin_content_id = {closed_origin_content_id} AND NOT (select count(*) from recommend where place_id = (select pid from (select place_id as pid from place  where origin_content_id = {closed_origin_content_id}) as a) and created_at>'{str_today}');"
        slack.concat_message(sql_delete)
        db.conn.execute(sql_delete)

    ### new 해시태그 한달 지나면 삭제
    a_month_ago = today - datetime.timedelta(30)
    str_a_month_ago = a_month_ago.strftime("%Y%m%d")
    sql_place_hashtag_delete = f"DELETE FROM place_hashtag WHERE created_at = '{str_a_month_ago}' and hashtag_id = 2050"  # 2050 == new
    slack.concat_message(sql_place_hashtag_delete)
    db.conn.execute(sql_place_hashtag_delete)

    slack.concat_message(
        (
            f"\n\n\u2705 성공 \n\n\n\n"
            "============================================================="
        )
    )
    slack.send_message_to_slack()
except Exception as e:
    slack.concat_message(
        (
            f"\n\n\U0001F62D 오류 발생:\n {e} \n\n\n\n"
            "============================================================="
        )
    )
    slack.send_message_to_slack()
    raise e
