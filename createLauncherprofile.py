"""
this is for creating  and ziping the gernerated output of minecrft 
"""

import json 

import pathlib
import re


def getGradleProjectVersion():
    version = 'version ='
    flag = 0
    index = 0
        
    with open('build.gradle','r') as data :
        print("opening gradle file")
        for line in data:

            line = line.split('=')
            line[0] = line[0].strip()

            if line[0] == 'version':
                project = re.sub(r'[]\[\' ]','', line[1].strip()).split(',')
                print("got project verion"+ " "+str(project[0]))

                return project[0]


def readBaseJson():

    with open('base.json') as json_file:
        data = json.load(json_file)
        return data


#* this is for finding, dumping and creating new launcher json file for new released version
def createlauncherjson():
    pass




def main():
   getGradleProjectVersion()





main()