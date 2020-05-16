import gspread
import os
from oauth2client.service_account import ServiceAccountCredentials

# Authorize api scopes based on creds file.
def AuthorizeApi():
    scope = ["https://spreadsheets.google.com/feeds","https://www.googleapis.com/auth/spreadsheets","https://www.googleapis.com/auth/drive.file","https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name("creds.json", scope)
    client = gspread.authorize(creds)
    return client

# Search for CSV files in CWD.
def SearchForCsv():
    CsvFiles = []
    for subdir, dirs, files in os.walk('./'):
        for filename in files:
            if filename.__contains__('.csv') :
                CsvFiles.append(filename)
    return CsvFiles

def ImportCsv( str, client ):
    content = open(str, 'r').read()
    client.import_csv('1iLw_Kum63F2xfP8-JC3Ih79tpKMvEBqg3cEkuYJGG6c', content)

def CleanUpFile( str ):
    os.remove(str)
    RemoveText = "Removed file: " + str
    print(RemoveText)

Files = SearchForCsv()
Result = any(Files)

# Only call AuthorizeApi and importCSV if there are actually files to import.
if Result:
    client = AuthorizeApi()
    for item in Files:
        ImportCsv(item, client)
        print('Import Complete.')
        CleanUpFile(item)
else:
    print('No items to import.')
