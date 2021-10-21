from sqlalchemy import create_engine
import config

db_connection_str = config.db_connection
db_connection = create_engine(db_connection_str)
conn = db_connection.connect()
