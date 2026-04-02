import gspread
import os
import json
from oauth2client.service_account import ServiceAccountCredentials
from pprint import pprint
import csv


# Authorize api scopes based on creds file.
def AuthorizeApi():
    scope = ["https://spreadsheets.google.com/feeds","https://www.googleapis.com/auth/spreadsheets","https://www.googleapis.com/auth/drive.file","https://www.googleapis.com/auth/drive"]
    creds = ServiceAccountCredentials.from_json_keyfile_name("creds.json", scope)
    client = gspread.authorize(creds)
    return client

# process data from Temp Sheet
def TempSheetProcessor() :
    tempsh = gc.open_by_key("1l3h1YMdtxWNS8_G2imsqtj562yBVjmRx36V7sY96S_w")
    tempSheet = tempsh.get_worksheet(0)
    tempData = tempSheet.get_all_values()
    return tempData

# Check for stub file; skip CSV parsing as data is already somewhere in google sheets.
def ForceUpdate():
    for subdir, dirs, files in os.walk('./'):
        for filename in files:
            if filename.__contains__('importNeeded.txt') :
                os.remove('importNeeded.txt')
                return True

# Search for CSV files in CWD.
def SearchForCsv():
    CsvFiles = []
    for subdir, dirs, files in os.walk('./'):
        for filename in files:
            if filename.__contains__('.csv') :
                CsvFiles.append(filename)
    return CsvFiles

# Import CSV file.
def ImportCsv( str ):
    client = AuthorizeApi()
    content = open(str, 'r').read()
    print(str)
    client.import_csv('1l3h1YMdtxWNS8_G2imsqtj562yBVjmRx36V7sY96S_w', content)
    with open("importNeeded.txt", "w+") as f:
        f.write("An import is needed, run sheet.py.")
    print("Wrote Stub File.")

# Clean up CSV files.
def CleanUpFiles( Files ):
    if any(Files):
        for item in Files:
            os.remove(item)
            RemoveText = "Removed file: " + item
            print(RemoveText)
    
# Only call AuthorizeApi and importCSV if there are actually files to import.
def ReadCsv( str ):
    content = []
    with open(str, newline='', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter=',')
        for row in reader:
            content = content + row
    return content

# start calls of support functions.
gc = AuthorizeApi()
sh = gc.open_by_key("1lslohutibUu5W-DU9hXE3PkvTBQ-1mUs5CKI5W85kd0") #AwesomeSheet

worksheet = sh.get_worksheet(0)
worksheetLayout = worksheet = sh.worksheet("layout")
worksheet_list = sh.worksheets()

data = worksheet.get_all_values()
layoutData = worksheetLayout.get_all_values()
Files = SearchForCsv()
Result = any(Files)
UpdateCheck = ForceUpdate()
# Only call AuthorizeApi and importCSV if there are actually files to import.
if UpdateCheck:
    try:
        print("Found stub file, update being forced from temp sheet.")
        worksheet = sh.worksheet("walmart")
        for item in Files:
            ImportCsv(item)
        fileName = item
        itemName = item.strip('.csv')
        try:
            worksheet = sh.add_worksheet(title=itemName, rows="100", cols="20")
        except:
            print("Worksheet already exists: {}".format(itemName))
        worksheet = sh.worksheet(itemName)
        worksheet.update(TempSheetProcessor())
    except:
        print("Error updating google sheets.")
elif Result:
    for item in Files:
        ImportCsv(item)
        fileName = item
        itemName = item.strip('.csv')
        try:
            worksheet = sh.add_worksheet(title=itemName, rows="100", cols="20")
        except:
            print("Worksheet already exists: {}".format(itemName))
        worksheet = worksheet = sh.worksheet(itemName)
        worksheet.update(TempSheetProcessor())
else:
    print('No items to import.')

CleanUpFiles(Files)
