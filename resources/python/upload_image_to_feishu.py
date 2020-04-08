#!/usr/bin/python
# -*- coding: UTF-8 -*-
import requests
import argparse
import json
import base64
import os

def get_args():
    parser = argparse.ArgumentParser('uploader')
    required = parser.add_argument_group('required arguments')
    required.add_argument('--path', help='json file path includes base64 string of qrcode image', required=True)
    required.add_argument('--url', help='upload url', required=True)
    return parser.parse_args()

def upload_image(path, url, token):
    with open(path, 'r') as f:
        image = json.load(f)

    code = image["base64"]
    decoded = base64.b64decode(code)

    response = requests.post(
        url=url,
        headers={'Authorization': f"Bearer {token}"},
        files={
            "image": decoded
        },
        data={
            "image_type": "message"
        },
        stream=True)

    response.raise_for_status()
    content = response.json()

    status_code = content["code"]
    message = content["msg"]

    if status_code == 0:
        return content["data"]["image_key"]
    else:
        raise Exception(f"ERROR: Failed to upload image, code {status_code}, message: {message}")

if __name__ == '__main__':
    args = get_args()
    token = os.getenv('TOKEN')
    upload_image(args.path, args.url, token)
