## 使用说明

这里就项目中需要添加和修改的主要位置和内容进行说明，详细设计参考[**设计文档**](https://github.com/WinterOrch/CL_TA/blob/mod/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3.md)。

#### Map类

每个独立进程（节点，即ProjectMain实例 ）的拓扑信息都是从Map类获得的，传递的信息包含三个部分，拓扑的具体建立过程可以参见[**设计文档**](https://github.com/WinterOrch/CL_TA/blob/mod/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3.md)。导入拓扑方法不限，在源码中位置见**TODO 3.0**，这里只对使用方法进行说明：

- ##### ArrayList\<Node> nodesInSystem

  需要包含系统中所有节点的信息（通过调用Node类构造方法传入序号（即进程在系统中的标识）、IP（可以是域名）、端口）。

  例：

  ```java
  ArrayList<Node> nodesInSystem = new ArrayList<>();
  Node node_1 = new Node(0,"192.168.43.43",8899);
  Node node_2 = new Node(1,"192.168.43.44",8899);
  Node node_3 = new Node(2,"192.168.43.45",8899);
  nodesInSystem.add(node_1);
  nodesInSystem.add(node_2);
  nodesInSystem.add(node_3);
  ```

- ##### int\[][] adjMatrix

  邻接矩阵，用于描述系统内的拓扑结构，通即为1，不通即为0。

  例如对于如下图所示的拓扑结构，
![image]
(img src="https://github.com/WinterOrch/CL_TA/blob/mod/%E7%A4%BA%E6%84%8F%E5%9B%BE.jpg)

  当甲，乙，丙分别对应序号0，1，2时，邻接矩阵应当如下所示：

  ```java
  int[][] adjMatrix = {
  {0,1,0},
  {1,0,1}
  {0,1,0}
  };
  ```

- ##### int currentNode

  即本进程（节点）在 **ArrayList\<Node> nodesInSystem** 中的**index**，用于指示本节点其信息在**nodesInSystem**中的位置，满足

  ```java
  Node thisNode = nodesInSystem.get(currentNode);
  ```

#### ChandyLamport类

ChandyLamport类用于封装进行快照协议会用到的方法，根据自己的设计进行编写，例如：

- sendMarkerMessage(ProjectMain projectMain, int neighbour)

  本节点向指定领节点发送Marker，见**TODO 2.0**。

- operateSnapping(ProjectMain projectMain, int neighbour)

  本节点执行快照操作并将快照发送给指定邻节点，见**TODO 2.1**。

  **注意：**ProjectMain中的枚举变量 **State currentState** 用于告知其它线程节点目前在快照还是正常工作，快照操作应当以状态的改变作为起始和结束，如下所示：

  ```java
  public static void operateSnapping(ProjectMain projectMain, int neighbour) {
  	projectMain.currentState = State.SNAPPING;
    ...
    ...
    sendMarkerMessage(projectMain,2);
    projectMain.currentState.WORKING;
  }
  ```

源码设想在收到Marker时（**TODO 1.0**）构造一个ChandyLamport线程（**TODO 1.1**），在新开的线程ChandyLamportThread中通过调用ChandyLamport类中的方法进行快照操作，这里也可以按个人理解进行修改。

#### ClientThread类

这个类是系统的数据接收线程，目前可能收到的包括以下两种信息

- ApplicationMsg

  转账信息，继承自Message基类，用于传输与转账业务相关的信息。

- MarkerMsg

  Marker信息，同样继承自Message基类。收到Marker后开启一个ChandyLamportThread，后在该线程中调用ChandyLamport类中方法，进行快照协议相关操作。

在源码中位置见**TODO 1.0** 。

#### ProjectMain类

本进程，其main函数就是系统主函数，详细设计参考[**设计文档**](https://github.com/WinterOrch/CL_TA/blob/mod/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3.md)。

对于没有服务器触发快照的设计，需要自行设计从0节点开始的快照触发，在源码中位置见**TODO 4.1**。



以下
