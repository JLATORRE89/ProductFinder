import gspread
import os
import json
from oauth2client.service_account import ServiceAccountCredentials
from pprint import pprint
import csv

scope = ["https://spreadsheets.google.com/feeds","https://www.googleapis.com/auth/spreadsheets","https://www.googleapis.com/auth/drive.file","https://www.googleapis.com/auth/drive"]

creds = ServiceAccountCredentials.from_json_keyfile_name("creds.json", scope)

gc = gspread.authorize(creds)
sh = gc.open_by_key("1lslohutibUu5W-DU9hXE3PkvTBQ-1mUs5CKI5W85kd0") #AwesomeSheet

worksheet = sh.get_worksheet(0)
worksheetLayout = worksheet = sh.worksheet("layout")
worksheet_list = sh.worksheets()

data = worksheet.get_all_values()
layoutData = worksheetLayout.get_all_values()

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
                return True

# Search for CSV files in CWD.
def SearchForCsv():
    CsvFiles = []
    for subdir, dirs, files in os.walk('./'):
        for filename in files:
            if filename.__contains__('.csv') :
                CsvFiles.append(filename)
    return CsvFiles

def ReadCsv( str ):
    content = []
    with open(str, newline='', encoding='utf-8') as f:
        reader = csv.reader(f, delimiter=',')
        for row in reader:
            content = content + row
    return content

UpdateCheck = ForceUpdate()
Files = SearchForCsv()
Result = any(Files)
# Only call AuthorizeApi and importCSV if there are actually files to import.
if UpdateCheck:
    try:
        print("Found stub file, update being forced from temp sheet.")
        worksheet = worksheet = sh.worksheet("walmart")
        worksheet.update(TempSheetProcessor())
    except:
        print("Error updating google sheets.")
elif Result:
    for item in Files:
        fileName = item
        itemName = item.strip('.csv')
        try:
            worksheet = sh.add_worksheet(title=itemName, rows="100", cols="20")
        except:
            print("Worksheet already exists: {}".format(itemName))
        content = ReadCsv(fileName)
        worksheet = sh.worksheet(itemName)
        worksheet.update(TempSheetProcessor())
else:
    print('No items to import.')
