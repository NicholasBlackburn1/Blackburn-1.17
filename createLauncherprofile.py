"""
this is for creating  and ziping the gernerated output of minecrft 
"""

import json 
import os 
import pathlib
import re



#* gets the project file  version
def getGradleProgjectName():

  with open('settings.gradle','r') as data :
       
        for line in data:

            line = line.split('=')
            line[0] = line[0].strip()

            if line[0] == 'rootProject.name':
                project = re.sub(r'[]\[\' ]','', line[1].replace('"',str())).split(',')
                print("got project name"+ " "+str(project[0]))

                return str(project[0])


#* gets the gradle projct version 
def getGradleProjectVersion():
    version = 'version ='

    with open('build.gradle','r') as data :
       
        for line in data:

            line = line.split('=')
            line[0] = line[0].strip()

            if line[0] == 'version':
                project = re.sub(r'[]\[\']','', line[1].rstrip().lstrip()).split(',')
                print("got project verion"+ " "+str(project[0]))

                return str(project[0])

#* reads the base json to create launcher file 
def readBaseJson():
    name = getGradleProgjectName().strip()+" "+getGradleProjectVersion()
    with open('base.json') as json_file:
        print("getting ready to write genetated")
        print("opening json file")
        data = json.load(json_file) 
        data['id'] =  name

        print( "UwU id "+data['id'])
        
        print("file name"+"profile/"+name+'.json')
        with open("profile/"+name+'.json', 'w') as outfile:
            json.dump(data, outfile,indent=4)

            


#* this is for finding, dumping and creating new launcher json file for new released version
def createlauncherjson():
    readBaseJson()
   
    
   




def main():
   
    createlauncherjson()
    




main()