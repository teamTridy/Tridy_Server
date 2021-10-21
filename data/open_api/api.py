import config

tour_service_key = config.tour_service_key
tour_service_key2 = config.tour_service_key2
tour_service_key3 = config.tour_service_key3

tour_base_url = f"http://api.visitkorea.or.kr/openapi/service/rest/KorService"

naver_base_url = "https://naveropenapi.apigw.ntruss.com/text-summary/v1/summarize"
naver_app_key_id = config.naver_app_key_id
naver_app_key = config.naver_app_key
naver_headers = {
    "Content-Type": "application/json; charset=utf-8",
    "X-NCP-APIGW-API-KEY-ID": naver_app_key_id,
    "X-NCP-APIGW-API-KEY": naver_app_key,
}

kakao_base_url = "https://dapi.kakao.com/v2/local"
kakao_rest_api_key = config.kakao_rest_api_key
kakao_headers = {
    "Authorization": f"KakaoAK {kakao_rest_api_key}",
}

tour_detail_intro_keywords_with_priority = {
    "contentid": ("콘텐츠 아이디", 3),
    "contenttypeid": ("콘텐츠 타입 아이디", 3),
    "accomcount": ("수용인원", 2),
    "chkbabycarriage": ("유모차대여 정보", 3),
    "chkcreditcard": ("신용카드가능 정보", 3),
    "chkpet": ("애완동물동반가능 정보", 3),
    "expagerange": ("체험가능 연령", 1),
    "expguide": ("체험안내", 1),
    "heritage1": ("세계 문화유산 유무", 3),
    "heritage2": ("세계 자연유산 유무", 3),
    "heritage3": ("세계 기록유산 유무", 3),
    "infocenter": ("문의 및 안내", 1),
    "opendate": ("개장일", 1),
    "parking": ("주차시설", 1),
    "restdate": ("쉬는날", 1),
    "useseason": ("이용시기", 1),
    "usetime": ("이용시간", 1),
    "accomcountculture": ("수용인원", 2),
    "chkbabycarriageculture": ("유모차대여 정보", 3),
    "chkcreditcardculture": ("신용카드가능 정보", 3),
    "chkpetculture": ("애완동물동반가능 정보", 3),
    "discountinfo": ("할인정보", 1),
    "infocenterculture": ("문의 및 안내", 1),
    "parkingculture": ("주차시설", 1),
    "parkingfee": ("주차요금", 1),
    "restdateculture": ("쉬는날", 1),
    "usefee": ("이용요금", 1),
    "usetimeculture": ("이용시간", 1),
    "scale": ("규모", 2),
    "spendtime": ("관람 소요시간", 2),
    "agelimit": ("관람 가능연령", 1),
    "bookingplace": ("예매처", 1),
    "discountinfofestival": ("할인정보", 1),
    "eventenddate": ("행사 종료일", 1),
    "eventhomepage": ("행사 홈페이지", 1),
    "eventplace": ("행사 장소", 1),
    "eventstartdate": ("행사 시작일", 1),
    "festivalgrade": ("축제등급 (2016-06-17 추가)", 2),
    "placeinfo": ("행사장 위치안내", 1),
    "playtime": ("공연시간", 1),
    "program": ("행사 프로그램", 1),
    "spendtimefestival": ("관람 소요시간", 1),
    "sponsor1": ("주최자 정보", 1),
    "sponsor1tel": ("주최자 연락처", 1),
    "sponsor2": ("주관사 정보", 1),
    "sponsor2tel": ("주관사 연락처", 1),
    "subevent": ("부대행사 내용", 1),
    "usetimefestival": ("이용요금", 1),
    "distance": ("코스 총거리 (신규항목)",),
    "infocentertourcourse": ("문의 및 안내",),
    "schedule": ("코스 일정",),
    "taketime": ("코스 총 소요시간 (신규항목)",),
    "theme": ("코스 테마",),
    "accomcountleports": ("수용인원", 2),
    "chkbabycarriageleports": ("유모차대여 정보", 3),
    "chkcreditcardleports": ("신용카드가능 정보", 3),
    "chkpetleports": ("애완동물동반가능 정보", 3),
    "expagerangeleports": ("체험 가능연령", 1),
    "infocenterleports": ("문의 및 안내", 1),
    "openperiod": ("개장기간", 1),
    "parkingfeeleports": ("주차요금", 1),
    "parkingleports": ("주차시설", 1),
    "reservation": ("예약안내", 1),
    "restdateleports": ("쉬는날", 1),
    "scaleleports": ("규모", 2),
    "usefeeleports": ("입장료", 1),
    "usetimeleports": ("이용시간", 1),
    "chkbabycarriageshopping": ("유모차대여 정보", 2),
    "chkcreditcardshopping": ("신용카드가능 정보", 2),
    "chkpetshopping": ("애완동물동반가능 정보", 3),
    "culturecenter": ("문화센터 바로가기", 2),
    "fairday": ("장서는 날", 1),
    "infocentershopping": ("문의 및 안내", 1),
    "opendateshopping": ("개장일", 1),
    "opentime": ("영업시간", 1),
    "parkingshopping": ("주차시설", 1),
    "restdateshopping": ("쉬는날", 1),
    "restroom": ("화장실 설명", 1),
    "saleitem": ("판매 품목", 2),
    "saleitemcost": ("판매 품목별 가격", 2),
    "scaleshopping": ("규모", 3),
    "shopguide": ("매장안내", 1),
    "chkcreditcardfood": ("신용카드가능 정보", 2),
    "discountinfofood": ("할인정보", 2),
    "firstmenu": ("대표 메뉴", 1),
    "infocenterfood": ("문의 및 안내", 1),
    "kidsfacility": ("어린이 놀이방 여부", 3),
    "opendatefood": ("개업일", 3),
    "opentimefood": ("영업시간", 1),
    "packing": ("포장 가능", 1),
    "parkingfood": ("주차시설", 1),
    "reservationfood": ("예약안내", 1),
    "restdatefood": ("쉬는날", 1),
    "scalefood": ("규모", 3),
    "seat": ("좌석수", 3),
    "smoking": ("금연/흡연 여부", 2),
    "treatmenu": ("취급 메뉴", 1),
    "lcnsno": ("인허가번호", 3),
}

category_hashtag_matching = {  # 2050 == new
    303: [2050, 155, 425, 363],
    304: [2050, 155, 425, 363],
    305: [2050, 155, 425, 363],
    306: [2050, 155, 1639, 1644, 363],
    307: [2050, 155, 153, 1644, 363],
    308: [2050, 155, 153, 1644, 363],
    309: [2050, 155, 153, 1644, 363],
    310: [2050, 155, 153, 363],
    311: [2050, 155, 153, 363],
    312: [2050, 155, 153, 363],
    313: [2050, 155, 153, 658, 363],
    314: [2050, 155, 153, 658, 363],
    315: [2050, 155, 153, 363],
    316: [2050, 155, 153, 363],
    317: [2050, 155, 658, 363],
    318: [2050, 155, 658, 363],
    319: [2050, 155, 153, 363],
    320: [2050, 155, 153, 363],
    321: [2050, 155, 1654, 401],
    322: [2050, 155, 363],
    323: [2050, 155, 363],
    324: [2050, 1825],
    325: [2050, 1825],
    326: [2050, 1825],
    327: [2050, 1825],
    328: [2050, 1825],
    329: [2050, 1825],
    330: [2050, 1825],
    331: [2050, 1825],
    332: [2050, 1825],
    333: [2050, 1825],
    334: [2050, 189, 153, 363],
    335: [2050, 189, 153, 363],
    336: [2050, 189, 153, 363, 401],
    337: [2050, 189, 153, 363, 1645],
    338: [2050, 189, 363],
    339: [2050, 425, 363],
    340: [2050, 425, 363],
    341: [2050, 189, 363],
    342: [2050, 189, 363],
    343: [2050, 189, 363],
    344: [2050, 189, 363],
    345: [2050, 189, 363],
    346: [2050, 189, 363],
    347: [2050, 189, 363],
    348: [2050, 189, 363, 401],
    349: [2050, 189, 363, 401],
    350: [2050, 189, 363, 401],
    351: [2050, 189, 363, 401],
    352: [2050, 189, 363, 401],
    353: [2050, 189, 363, 401],
    354: [2050, 189, 363, 401],
    355: [2050, 189, 363, 401],
    356: [2050, 189, 363, 401],
    357: [2050, 189, 363, 401],
    358: [2050, 155, 363],
    359: [2050, 155, 363, 401],
    360: [2050, 155, 363],
    361: [2050, 155, 363],
    362: [2050, 155, 363],
    363: [2050, 155, 363],
    364: [2050, 1655, 535, 1645, 401],
    365: [2050, 1766, 535, 1645, 401],
    366: [2050, 1766, 535, 1645, 401],
    367: [2050, 1766, 535, 1645, 401],
    368: [2050, 1655, 535, 1645, 401],
    369: [2050, 1748, 535, 1645, 401],
    370: [2050, 1796, 535, 1645, 401],
    371: [2050, 1796, 535, 1645, 401],
    372: [2050, 1784, 535, 1645, 401],
    373: [2050, 1865, 535, 1645, 401],
    374: [2050, 1796, 535, 1645, 401],
    375: [2050, 1796, 535, 1645, 401],
    376: [2050, 1796, 401],
    377: [2050, 1796, 401],
    378: [2050, 2044, 401],
    379: [2050, 2044, 401],
    380: [2050, 1748, 401],
    381: [2050, 1748, 535, 1645, 401],
    382: [2050, 1748, 535, 1645, 401],
    383: [2050, 1748, 535, 1645, 401],
    384: [2050, 1749, 535, 1645, 401],
    385: [2050, 1749, 535, 1645, 401],
    386: [2050, 1749, 535, 1645, 401],
    387: [2050, 1748, 535, 1645, 401],
    388: [2050, 1748, 535, 1645, 401],
    389: [2050, 1748, 535, 1645, 401],
    390: [2050, 1749, 535, 1645, 401],
    391: [2050, 1749, 363],
    392: [2050, 1749, 363],
    393: [
        2050,
    ],
    394: [
        2050,
    ],
    395: [
        2050,
    ],
    396: [2050, 154, 363],
    397: [2050, 154, 363],
    398: [2050, 154, 363],
    399: [2050, 154, 363],
    400: [2050, 154, 363],
    401: [2050, 154, 363],
    402: [2050, 154, 363],
    403: [2050, 154, 363],
    404: [2050, 154, 363],
    405: [2050, 154, 535, 401, 1645],
    406: [2050, 154, 363],
    407: [2050, 154, 363],
    408: [2050, 154, 363],
    409: [2050, 154, 363],
    410: [2050, 154, 363],
    411: [2050, 154, 363],
    412: [2050, 154, 363],
    413: [2050, 154, 363],
    414: [2050, 154, 363],
    415: [2050, 154, 363],
    416: [2050, 154, 363],
    417: [2050, 154, 363],
    418: [2050, 154, 363],
    419: [2050, 154, 363],
    420: [2050, 154, 363],
    421: [2050, 154, 363],
    422: [2050, 154, 363],
    423: [2050, 154, 363],
    424: [2050, 154, 363],
    425: [2050, 154, 363],
    426: [2050, 154, 363],
    427: [2050, 154, 363],
    428: [2050, 154, 363],
    429: [2050, 154, 363],
    430: [2050, 154, 363],
    431: [2050, 154, 363],
    432: [2050, 154, 363],
    433: [2050, 154, 363],
    434: [2050, 154, 363],
    435: [2050, 154, 363],
    436: [2050, 1664, 1930, 363, 401],
    437: [2050, 1664, 1930, 363, 401],
    438: [2050, 1664, 363, 401, 1645],
    439: [2050, 1664, 103, 363, 401, 1645],
    440: [2050, 1664, 363, 401, 1645],
    441: [2050, 1664, 363, 401, 1645],
    442: [2050, 1664, 103, 363, 401, 1645],
    443: [2050, 1664, 103, 363, 401, 1645],
    444: [2050, 1664, 103, 363, 401, 1645],
    445: [2050, 1167, 363, 401, 1645],
    446: [2050, 1167, 363, 401, 1645],
    447: [2050, 1167, 363, 401, 1645],
    448: [2050, 1167, 363, 401, 1645],
    449: [2050, 1167, 363, 401, 1645],
    450: [2050, 1167, 363, 401, 1645],
    451: [2050, 1167, 363, 401, 1645],
    452: [2050, 1167, 363, 401, 1645],
    453: [2050, 2, 363, 401, 1645],
    454: [2050, 1167, 363, 401, 1645],
}
