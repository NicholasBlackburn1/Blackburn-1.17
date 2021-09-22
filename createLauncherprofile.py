"""
this is for creating  and ziping the gernerated output of minecrft 
"""

import json 

import pathlib
import re


def getGradleProgjectName():

  with open('settings.gradle','r') as data :
       
        for line in data:

            line = line.split('=')
            line[0] = line[0].strip()

            if line[0] == 'rootProject.name':
                project = re.sub(r'[]\[\' ]','', line[1].replace('"','')).split(',')
                print("got project name"+ " "+str(project[0]))

                return project[0]



def getGradleProjectVersion():
    version = 'version ='
    flag = 0
    index = 0
        
    with open('build.gradle','r') as data :
       
        for line in data:

            line = line.split('=')
            line[0] = line[0].strip()

            if line[0] == 'version':
                project = re.sub(r'[]\[\' ]','', line[1].strip()).split(',')
                print("got project verion"+ " "+str(project[0]))

                return project[0]


def readBaseJson():

    with open('base.json') as json_file:
        print("getting ready to write genetated")
        print("opening json file")
        data = json.load(json_file) 
        data['id'] =  getGradleProgjectName()+" "+getGradleProjectVersion()

        print( data['id'])
        
        if data['id'] ==  getGradleProgjectName()+" "+getGradleProjectVersion():
            with open('test.json', 'w') as outfile:
                json.dump(data, outfile,indent=4)

            


#* this is for finding, dumping and creating new launcher json file for new released version
def createlauncherjson():
    pass




def main():
   readBaseJson()





main()