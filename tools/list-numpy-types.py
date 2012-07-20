import numpy
from docscrape import NumpyDocString
from inspect import *
import re

def extractTypes(docstring):
    types = []
    nds = NumpyDocString("    " + docstring)
    for _, type, _ in nds["Parameters"]:
        m = re.match(re.compile("^(.*), optional$"), type)
        if m:
            matchedType = m.groups()[0]
            if not matchedType is None:
                types.append(matchedType)
        else:
            types.append(type)
    for _, type, _ in nds["Returns"]:
        types.append(type)
    return types

def collectTypes():
    types = []
    for _, f in getmembers(numpy):
        if isroutine(f):
            if f.__doc__:
                types.extend(extractTypes(f.__doc__))
        elif isclass(f):
            for _, m in getmembers(f):
                if isroutine(m):
                    if m.__doc__:
                        types.extend(extractTypes(m.__doc__))
    return types

def listRoutines():
    print "Functions:"
    for m in getmembers(numpy):
        if isroutine(m[1]):
            print m[0]
            print m[1].__doc__
    print ""

def listClasses():
    print "Classes:"
    for m in getmembers(numpy):
        if isclass(m[1]):
            print m[0]
    print ""

types = collectTypes()
typesDict = {}
for type in types:
    if type in typesDict:
        typesDict[type] += 1
    else:
        typesDict[type] = 1


for type, count in sorted(typesDict.iteritems(), key=lambda (k,v): (v,k), reverse=True):
    print type + ": " + str(count)

