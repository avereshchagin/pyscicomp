# PySciComp
Plugin that adds support for Python scientific computing libraries to PyCharm.

## Main Features

* Support for Numpy docstring format.
* Support for user-defined types database.
* Support for Numpy dynamic types.
* Autocompletion and inspection for functions accepting arguments from a finite set of values.

## Building the Plugin

1. Clone the project repository:

``` bash
git clone git://github.com/avereshchagin/pyscicomp.git pyscicomp
```

2. Start IntelliJ IDEA and create an empty project without creating any modules.
3. Open _Project Structure_ window (File -> Project Structure...) and select _Platform Setings/SDKs_ panel.
4. Click on the plus button and add new _IntelliJ IDEA Plugin SDK_, choose the path to PyCharm build you want to use and set the name of the SDK to _PyCharm SDK_.
5. Select _Project Settings/Project_ panel and choose created on the previous step _PyCharm SDK_ as _Project SDK_.
6. Select _Project Settings/Modules_ panel and click on the plus button to add a module.
7. In _Add Module_ dialog select _Import existing module_ and provide the path to _pyscicomp.iml_ file from the project repository.
8. Press OK to apply all changes and close settings window.
9. Select menu _Run -> Edit Configurations_, press the plus button to add new run configuration and choose _Plugin_ in the list.
10. Leave default settings and press OK.
11. Now you can start PyCharm with the plugin choosing menu _Run -> Run_.
