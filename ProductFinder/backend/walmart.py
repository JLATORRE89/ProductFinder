from bs4 import BeautifulSoup
from datetime import datetime
import json
import urllib3
import requests
import csv
import os


# Input from user or service (case sensitive)
SearchTerm = input('Enter Product to search for: ')
SearchTerm = SearchTerm.lower()

# Format and query URL with user input
SearchSplit = SearchTerm.split(' ')
SearchMark = SearchTerm.replace(' ', '+')
#url = 'https://www.walmart.com/search/?page=1&ps=40&query=' + SearchMark
url = 'https://www.walmart.com/search/?cat_id=0&query=' + SearchMark
body = {'location-data': '55555'}
headers = {'content-type': 'application/json'}

print(url)

website = requests.get(url).text
website = website.lower()
soup = BeautifulSoup(website, 'lxml')

# process the status of a single product.
def ProductStatus( itemUrl ):
    productWebsite = requests.get(itemUrl).text
    productSoup = BeautifulSoup(productWebsite, 'lxml')
    itemStatus = productSoup.find(class_='prod-fulfillment-messaging-text')
    itemFulfillment = productSoup.find(class_='prod-fulfillment')
    if itemFulfillment is not None:
        itemFulfillmentText = itemFulfillment.text
        itemFulfillmentText = itemFulfillmentText.replace("ordersArrives", "orders. Arrives")
        itemFulfillmentText = itemFulfillmentText.replace("onlyIn", "only. In")
        itemFulfillmentText = itemFulfillmentText.replace("NextDay", "Next-Day")
    elif itemStatus is not None :
        itemFulfillmentText = itemStatus.text
        itemFulfillmentText = itemFulfillmentText.replace("ordersArrives", "orders. Arrives")
        itemFulfillmentText = itemFulfillmentText.replace("onlyIn", "only. In")
        itemFulfillmentText = itemFulfillmentText.replace("NextDay", "Next-Day")
    else:
        itemFulfillmentText = "No Shipping Data."
    return itemFulfillmentText

# Write CSV file for end user consumption.
with open('walmart.csv', 'w', newline='') as csv_file:
    csv_writer = csv.writer(csv_file)
    Timetamp = datetime.now()
    csv_writer.writerow(['item', 'url', 'status','timestamp'])
    for item in soup.find_all(class_='product-title-link line-clamp line-clamp-2 truncate-title'):
        if item.text.__contains__(SearchTerm) :
            print(item)
            text = item.text.replace(",", '')
            itemUrl = 'https://www.walmart.com' + item.get('href')
            itemStatus = ProductStatus(itemUrl)
            print(itemUrl)
            csv_writer.writerow([text, itemUrl, itemStatus, Timetamp])
            with open("importNeeded.txt", "w+") as f:
                f.write("An import is needed, run importSheets.py.")
                print("Wrote Stub File.")
