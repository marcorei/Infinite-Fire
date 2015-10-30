# Infinite Fire

A small library that helps you connecting Firebase to a RecyclerView.

Inspired by the official [FirebaseUI-Android library](https://github.com/firebase/FirebaseUI-Android). 

## Demo

To run the demo you'll need a Firebase reference with chat messages similar to [FirebaseUI-Android](https://github.com/firebase/FirebaseUI-Android). 
Enter the URL in the [string resource file](https://github.com/marcorei/Infinite-Fire/blob/master/app/src/main/res/values/strings.xml#L2).

## How to use Infinite Fire

The core of Infinite Fire is the InfiniteFireArray.
```java
InfiniteFireArray array = new InfiniteFireArray(

        // Your Firebase Query.
        // Do NOT use Query.limitToFirst() or Query.limitToLast(), InfiniteFireArray will do that for you.
        // Do sort your Query by using Query.orderByKey() or Query.orderByString(String)
        query, 

        // The number of inital items.
        // InfiniteFireArray will limit the Query using this int.
        20,

        // This number of items loaded each time you call InfiniteFireArray.more().
        // InfiniteFireArray will use this number to raise the limit of the initial Query.
        20,

        // Defines if your Query will be limited using Query.limitToFirst() or Query.limitToLast()
        // In most cases you want this to be false to load the newest items first.
        false,

        // Defines if InfiniteFireArray changes the order of the items after the inital load.
        // For more info see below.
        false
);
```

Then implement InfiniteFireRecyclerViewAdapter and pass the InfiniteFireArray as parameter.
```java
class MyRecyclerView extends InfiniteFireRecyclerViewAdapter {...}
MyRecyclerView recyclerView = new MyRecyclerView(array);
```

For more information take a look at the examples.


## Features

### Scroll-to-load-more.
Use ```java InfiniteFireArray.more()``` to implement a scroll-to-load-more pattern. 
It raises the limit of your Query incrementally.

### Pull-to-refresh.
Firebase offers real-time functionality. 
InfiniteFireArray uses the ```java Query.addChildEventListener()``` to keep your data up to date.

There are use-cases though in which constantly changing the order of your items results in a really bad user experience. 
Think about a grid view that is sorted by a value that changes a lot, e.g. a comments counter -- your view would look like a memory game.

InfiniteFireArray allows you two maintain the order while still updating the existing items in real-time.
Implementing a ```java SwipeRefreshLayout``` allows you to call ```java InfiniteArray.reset()``` to "reload" the data and set the limit of the Query to its orignial limit.

### Loading events.
Attach a ```java InfiniteFireArray.OnLoadingStatusListener``` to receive loading events. 

Please note that these event only indicate when the inital sync is completed. 
InfiniteFireArray will still receive real-time updates without dispatching further events after the inital sync.

## How to install Infinite Fire

Get it from [Jitpack](https://jitpack.io/#marcorei/Infinite-Fire/) via Gradle or Maven.

## Thanks
- [FirebaseUI-Android library](https://github.com/firebase/FirebaseUI-Android) Inspiration for this library, uses an Array to connect Firebase to RecyclerViews
- [Kato](https://twitter.com/katowulf) Great support, thank you!
