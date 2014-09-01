//package com.tian.test;
//
////Java 2 平台引入了 java.lang.ref 包，其中包括的类可以让您引用对象，而不将它们留在内存中。这些类还提供了与垃圾收集器（garbage collector）之间有限的交互。
////
////先“由强到弱”（只的是和垃圾回收器的关系）明确几个基本概念：
////strong references是那种你通常建立的reference，这个reference就是强可及的。这个不会被垃圾回收器自动回收。例如：
////StringBuffer buffer = new StringBuffer();
////其中这个buffer就是强引用，之所以称为“强”是取决于它如何处理与Garbage Collector的关系的：它是无论如何都不会被回收的。够强的。强引用在某些时候是有个问题的，下边的一个哈希表实例就是很好的说明。而且还有一个问题就是在缓冲上，尤其是诸如图片等大的结构上。我们在内存中开辟一块区域放置图片缓冲，那我们就希望有个指针指向那块区域。此时若是使用强引用则会强迫图片留在内存，当你觉得不需要的时候你需要手动移除，否则就是内存泄漏。
////
////WeakReference则类似于可有可无的东西。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存，说白了就是一个没那么strong要求垃圾回收器将一个对象保留在内存中。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象。常说的Unreachable和弱引用指代的是一个意思。这可能还是说不清楚，那么我举个例子：
////你有一个类叫做Widget，但是由于某种原因它不能通过继承来添加一项功能。当我们想从这个对象中取出一些信息的时候怎么办呢？假设我们需要监视每个 Widget的serial Number，但是这个Widget却偏偏没有这个属性，而且还不可继承...这时候我们想到了用 HashMaps：serialNumberMap.put(widget, widgetSerialNumber);
//// 这不就截了嘛~表面上看起来是ok的，但是正是Widget这个Strong Reference产生了问题。当我们设定某个Widget的SerialNumber不需要的时候，那么要从这个映射表中除去这个映射对，否则我们就有了内存泄漏或者是出错（移除了有效的SerialNumber）。这个问题听起来很耳熟，是的，在没有垃圾管理机制的语言中这是个常见问题，在JAVA中我们不用担心。因为我们有WeakReference。我们使用内置的WeakHashMap类，这个类和哈希表HashMap几乎一样，但就是在键 key的地方使用了WeakReference，若一个WeakHashMap key成为了垃圾，那么它对应的入口就会自动被移除。这就解决了上述问题~
////
////SoftReference则也类似于可有可无的东西。如果内存空间足够，垃圾回收器就不会回收它，如果内存空间不足了，就会回收这些对象的内存。只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。
////
////弱引用与软引用的区别在于：具有WeakReference的对象拥有更短暂的生命周期。或者说SoftReference比WeakReference对回收它所指的对象不敏感。一个WeakReference对象会在下一轮的垃圾回收中被清理，而SoftReference对象则会保存一段时间。SoftReferences并不会主动要求与 WeakReference有什么不同，但是实际上SoftReference对象一般在内存充裕时一般不会被移除，这就是说对于创建缓冲区它们是不错的选择。它兼有了StrongReference和WeakReference的好处，既能停留在内存中，又能在内存不足是去处理，这一切都是自动的！
////
////PhantomReference为"虚引用"，顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收，也就是说其get方法任何时间都会返回null。虚引用主要用来跟踪对象被垃圾回收的活动。其必须和引用队列（ReferenceQueue）联合使用，这是与弱引用和软引用最大的不同。     
////
////WeakReference是在垃圾回收活动之前将对象入队的，理论上讲这个对象还可以使用finalize()方法使之重生，但是WeakReference仍然是死掉了。 PhantomReferences对象是在对象从内存中清除出去的时候才入队的。也就是说当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之关联的引用队列中。程序可以通过判断引用队列中是否已经加入了虚引用，来了解被引用的对象是否将要被垃圾回收。程序如果发现某个虚引用已经被加入到引用队列，那么就可以在所引用的对象的内存被回收之前采取必要的行动。它限制了finalize()方法的使用，更安全也更高效。
////
////我们看看这个包给我们提供了什么类？
////WeakReference 类
////WeakReference weakref = new WeakReference(ref);
////这样 weakref 就是 ref 指向对象的一个 weak reference。要引用这个 weak reference 指向的对象可以用 get 方法。把对象的 weak reference 放入 Hashtable 或者缓存中，当没有 strong reference 指向他们的时候，对象就可以被垃圾收集器回收了。实际上，有一个 WeakHashMap 就是专门做这个事的。一旦WeakReference使用get方法返回null的时候，它指向的对象已经变成了垃圾，这个weakref对象也没什么用处了。这就需要有一些清理工作了。而ReferenceQueue类就是做这个的，要是你向ReferenceQueue类传递了一个 WeakReference的构造方法，那么当引用所指的对象成为垃圾时，这个引用的对象就会被自动插入到这个引用队列中。你可以在一定时间间隔内处理这个队列。
////
////SoftReference 类　
////可用来实现智能缓存（java.lang.ref.SoftReference is a relatively new class, used to implement smart caches.）
////
////假定你有一个对象引用，指向一个大数组：
////
////Object obj = new char[1000000];
////并且如果可能的话，你打算一直保存这个数组，但是如果内存极其短缺的话，你乐于释放这个数组。你可以使用一个
////soft reference:
////SoftReference ref = new SoftReference(obj);
////Obj是这个soft reference的引用。在以后你用以下的方式检测这个引用：
////if (ref.get() == null)// (referent has been cleared)
////else// (referent has not been cleared)
//// 如果这个引用已经被清除了，那么垃圾回收器会收回它所使用的空间，并且你缓存的对象也已经消失。需要注意的是，如果这个指示物还有对它的别的引用，那么垃圾回收器将不会清除它。这个方案可以被用来实现各种不同类型的缓存，这些缓存的特点是只要有可能对象就会被一直保存下来，但是如果内存紧张对象就被清除掉。
////注意：软引用可以和一个引用队列（ReferenceQueue）联合使用，如果软引用所引用的对象被垃圾回收，Java虚拟机就会把这个软引用加入到与之关联的引用队列中。
////
////e.g.
//
//import java.lang.ref.*;  
// 
//public class testRef {  
//	
//  public static void main(String[] args) {  
//    Object weakObj, phantomObj;  
//    Reference ref;  
//    WeakReference weakRef;  
//    PhantomReference phantomRef;  
//    ReferenceQueue weakQueue, phantomQueue;  
// 
//    weakObj    = new String("Weak Reference");  
//    phantomObj = new String("Phantom Reference");  
//    weakQueue    = new ReferenceQueue();  
//    phantomQueue = new ReferenceQueue();  
//    weakRef    = new WeakReference(weakObj, weakQueue);  
//    phantomRef = new PhantomReference(phantomObj, phantomQueue);  
// 
//    // Print referents to prove they exist.  Phantom referents  
//    // are inaccessible so we should see a null value.  
//    System.out.println("Weak Reference: " + weakRef.get());  
//    System.out.println("Phantom Reference: " + phantomRef.get());  
// 
//    // Clear all strong references  
//    weakObj    = null;  
//    phantomObj = null;  
// 
//    // Invoke garbage collector in hopes that references  
//    // will be queued  
//    System.gc();  
// 
//    // See if the garbage collector has queued the references  
//    System.out.println("Weak Queued: " + weakRef.isEnqueued());  
//    // Try to finalize the phantom references if not already  
//    if(!phantomRef.isEnqueued()) {  
//      System.out.println("Requestion finalization.");  
//      System.runFinalization();  
//    }  
//    System.out.println("Phantom Queued: " + phantomRef.isEnqueued());  
// 
//    // Wait until the weak reference is on the queue and remove it  
//    try {  
//      ref = weakQueue.remove();  
//      // The referent should be null  
//      System.out.println("Weak Reference: " + ref.get());  
//      // Wait until the phantom reference is on the queue and remove it  
//      ref = phantomQueue.remove();  
//      System.out.println("Phantom Reference: " + ref.get());  
//      // We have to clear the phantom referent even though  
//      // get() returns null  
//      ref.clear();  
//    } catch(InterruptedException e) {  
//      e.printStackTrace();  
//      return;  
//   }  
// }  
//}
//
//
