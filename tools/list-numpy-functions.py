import numpy
from docscrape import NumpyDocString
from inspect import *
import re

def listRoutines():
    print "Functions:"
    for m in getmembers(numpy):
        if isroutine(m[1]) or type(m[1]) == numpy.ufunc:
            print "numpy." + m[0] + "()"
    print ""

def listClasses():
    print "Classes:"
    for m in getmembers(numpy):
        if isclass(m[1]):
            print m[0]
    print ""

listRoutines()
