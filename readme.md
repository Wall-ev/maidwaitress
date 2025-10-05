## 工作原理
使用机械动力的剪切板作为菜单，喝啤酒啦的唤餐铃呼叫服务员。

顾客使用唤餐铃呼叫服务员: [CallerWaiterEvent](./src/main/java/com/github/wallev/carvein/event/CallerWaiterEvent.java) <br>
-> 服务员过来给顾客菜单: [DeliverFoodMenuTask](./src/main/java/com/github/wallev/carvein/waitress/behavior/DeliverFoodMenuTask.java) <br>
-> 顾客点餐，标记餐桌，并将菜单返回给服务员: [BackOrder2WaiterEvent](./src/main/java/com/github/wallev/carvein/event/MarkRequestTablePosEvent.java) [BackOrder2WaiterEvent](./src/main/java/com/github/wallev/carvein/event/BackOrder2WaiterEvent.java) <br>
-> 服务员回去通知厨师: [BackOrder2WaiterEvent](./src/main/java/com/github/wallev/carvein/waitress/behavior/DeliverOrder2ChefTask.java) <br>
-> 等待厨师制作食物: [BackOrder2WaiterEvent](./src/main/java/com/github/wallev/carvein/event/MarkAlreadyMakeFoodEvent.java) <br>
-> 上菜: [TakeFood2TableTask](./src/main/java/com/github/wallev/carvein/waitress/behavior/TakeFood2TableTask.java) / [TakeFood2PlayerTask](./src/main/java/com/github/wallev/carvein/waitress/behavior/TakeFood2PlayerTask.java) <br>

当然，本示例还没有做细节处理，比如厨师做好食物后是通过丢食物、服务员捡起的方式运行的，本示例并没有做等到捡起食物再上菜的处理，可以自行处理；<br>
因为仓管还没有给将制作好的物品递给目标是实体的事件，所以先使用mixin触发自己写的事件(可以对着小鱼急急急）);<br>
也还没有做多人处理；<br>
这些就交给你们处理啦qwq，只是简单写个示例，方便你们魔改开发理解。<br>