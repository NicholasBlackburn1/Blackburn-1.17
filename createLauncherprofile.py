"""
this is for creating  and ziping the gernerated output of minecrft 
"""

import json 
import pathlib


#* this is for finding, dumping and creating new launcher json file for new released version
def createlauncherjson():

    with open('base.json') as json_file:
        data = json.load(json_file)
        print(data['id'])




def main():
    createlauncherjson()





main()