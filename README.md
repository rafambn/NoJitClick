# ClickManager [![](https://jitpack.io/v/rafambn/ClickManager.svg)](https://jitpack.io/#rafambn/ClickManager)
ClickManager is designed to prevent unintended double-click events
## The problem
If you have an activity where a button starts another activity, for example, if you accidentally click the button multiple times, it will start the activity several times.
<p align="center"><img src="https://github.com/rafambn/ClickManager/blob/master/arts/startActivity.png" /></p>
This happens because the clicks are queued to be executed, but if this causes logic problems, like in the example, you will need to handle it in your code. The problem can also occur if your action executes something asynchronously. This project was developed with the intention of preventing such problems in a simple way for the user.
## How to install

# Gradle
1. Add the JitPack repository to your build file in your root build.gradle at the end of repositories:
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the dependency:
```groovy
dependencies {
	implementation 'com.github.rafambn:ClickManager:Tag'
}
```
# How it works
<p align="center"><img src="https://github.com/rafambn/ClickManager/blob/master/arts/viewGroups.png" /></p>
In the image above, the blue spots are from a different viewGroup than the red ones, which means that if both blue or red buttons are pressed, only one will be executed, while if a blue and a red button are clicked, both will be executed.
# How to use
1. Declare the ClickManager variable:
```kotlin
lateinit var clickManager: ClickManager
```
2. Annotate the views and interfaces that you want to manage:
```kotlin
@ManageClick(gruopId = 0, isAsync = false, minClickInterval = 1000L)
lateinit var button: Button
@ManageClick(gruopId = 0, isAsync = false, minClickInterval = 1000L)
lateinit var myInterface: MyInterface
```
 *The default values of ManageClick are the ones described above.
  
3. Instantiate the ClickManager:
```kotlin
myInterface = object : MyInterface {
	override fun onClick() {
		// some code here
	}

	override fun onLongClick() {
		// some other code here
	}
}
button.setOnClickListener { 
	// some other other code xD
}
clickManager = ClickManager(this, 0)
myAdapter.setListener(myInterface)
```
 *If you use hierarchy on the activity you must instantiate the ClickManager providing the numbers of parent classes that the annotation is present

4. For an async method or methods that have a callback, you must pass a Runnable to be executed when the task its done and also annotate the field as async:
```kotlin
@ManageClick(gruopId = 0, isAsync = true, minClickInterval = 1000L)
lateinit var button: Button
button.setOnClickListener { doesAsyncStuff(clickManager.getUnblocker(0)) }
suspend fun(unBlocker: Runnable) {
	// Does some async stuff
	unBlocker.run()
}
```
# Limitations
- The ClickManager must be instantiated after all listeners are set to its views and interfaces are instantiate but before any manipulation of interfaces.
- It will only manage clicks of the “onClick” methods of the interfaces. Further improvements will allow it to manage other methods of interfaces.
- If the listeners of the view change it will not work
- It is designed to only work in views activities, it wasn’t tested or designed to work with Compose.

#License
-------
    Copyright (c) 2023 Rafael.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

   
