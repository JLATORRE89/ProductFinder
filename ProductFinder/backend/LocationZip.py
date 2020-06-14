import gspread
import os
import requests
import sqlite3
from oauth2client.service_account import ServiceAccountCredentials

os.chdir("/home/dev/ProductFinder/backend/")
# Authorize api scopes based on creds file.
def AuthorizeApi():
    scope = ["https://spreadsheets.google.com/feeds","https://www.googleapis.com/auth/spreadsheets","https://www.googleapis.com/auth/drive.file","https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name("creds.json", scope)
    client = gspread.authorize(creds)
    return client

# start calls of support functions.
gc = AuthorizeApi()
sh = gc.open_by_key("1lslohutibUu5W-DU9hXE3PkvTBQ-1mUs5CKI5W85kd0") #AwesomeSheet

worksheetResponse = sh.worksheet("Form Responses 1")
worksheet_list = sh.worksheets()

data = worksheetResponse.get_all_values()
DataCheck = any(data)
ZipCodeList = []

# Only call AuthorizeApi and importCSV if there are actually files to import.
if DataCheck:
    try:
        print("Found data.")
        for item in data:
            itemStr = str(item)
            itemStr = itemStr.replace("'", "")
            itemStr = itemStr.replace("]", " ")
            itemStr = itemStr.split(',')
            ZipCodeList.append(itemStr[1])
    except:
        print("Error processing google sheets data.")
else:
    print('No data found.')

def DatabaseUpdate(ZipItem, Response):
    for subdir, dirs, files in os.walk('./'):
        if files.__contains__('location.db'):
            print("Found database file.")
            conn = sqlite3.connect('location.db')
            db = conn.cursor()
            db.execute("INSERT INTO locations VALUES ('ZipItem','Response')")
            conn.commit()
        else:
            print("No Database found, creating one.")
            conn = sqlite3.connect('location.db')
            db = conn.cursor()
            #Create Table
            db.execute('''CREATE TABLE locations (zip text, location text)''')
            db.execute("INSERT INTO locations VALUES ('ZipItem','Response')")
            # Save (commit) the changes
            conn.commit()


for ZipItem in ZipCodeList:
    if not ZipItem.__contains__("INPUT!!"):
        url = 'https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&q=' + ZipItem
        Response = requests.get(url).text
        Response = Response.lower()
        NewData = f'{ZipItem} - {Response}'
        DatabaseUpdate(ZipItem, Response)
        print(NewData)
