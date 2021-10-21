import db
import pandas as pd


def get_need_to_clean_hashtag_list():
    sql_select = f"SELECT hashtag_id, name FROM hashtag where name like ' %%'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return [
        (result.loc[i, "hashtag_id"], result.loc[i, "name"]) for i in range(len(result))
    ]


def get_need_to_clean_hashtag_id_list():
    sql_select = f"SELECT hashtag_id, name FROM hashtag where name like ' %%'"
    result = pd.read_sql_query(sql_select, db.db_connection)
    return [result.loc[i, "hashtag_id"] for i in range(len(result))]


def get_cleansed_hashtag_matching_list(hashtag_list):
    cleansed_hashtag_matching_list = list()
    for hashtag in hashtag_list:
        sql_select = f"select hashtag_id, name from tridy.hashtag where name = (SELECT replace(name, ' ', '') FROM tridy.hashtag where hashtag_id = {hashtag[0]})"
        result = pd.read_sql_query(sql_select, db.db_connection)
        if len(result) > 0:
            cleansed_hashtag_matching_list.append(
                (hashtag[0], result.loc[0, "hashtag_id"])
            )
    return cleansed_hashtag_matching_list


def update_to_matched_hashtag(cleansed_hashtag_matching_list):
    for hashtag_matching in cleansed_hashtag_matching_list:
        sql_update = f"UPDATE place_hashtag SET hashtag_id = {hashtag_matching[1]} WHERE hashtag_id = {hashtag_matching[0]}"
        print(sql_update)
        db.conn.execute(sql_update)


def update_to_clenased_hashtag(need_to_clean_hashtag_list):
    for hashtag in need_to_clean_hashtag_list:
        sql_update = f"UPDATE hashtag SET name = REPLACE(name,' ','') WHERE hashtag_id = {hashtag[0]}"
        print(sql_update)
        db.conn.execute(sql_update)


hashtag_list = get_need_to_clean_hashtag_list()
update_to_clenased_hashtag(hashtag_list)
