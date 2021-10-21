import requests
import json
import config


class Slack(object):
    def __init__(self, channel_url: str):
        self.channel_url = channel_url
        self.message = ""

    def concat_message(self, message: str):
        self.message += message + "\n"

    def send_message_to_slack(self):
        response = requests.post(
            self.channel_url,
            data=json.dumps({"text": self.message}),
            headers={"Content-Type": "application/json"},
        )
        print(response)


def generate(channel):
    if channel == "delete":
        channel_url = config.slack_delete_channel_url
    else:
        channel_url = config.slack_insert_update_channel_url

    if channel_url:
        slack = Slack(channel_url)
        return slack