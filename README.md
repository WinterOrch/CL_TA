Chandy-Lamport实验平台

整个系统只有一项业务，就是转钱，围绕钱的一切操作都在金库类Cache中进行，一个进程（节点）维护一个静态的Cache实例。

整个系统只是一个简易的案例，可以在此基础上增加快照协议完成实验。

开发说明

- 主进程
  ProjectMain类是进程类，即节点类。
  ProjectMain的状态通过一个枚举类State表示，用于区分快照状态和普通状态。
  与邻节点的信道通过HashMap<Integer,Socket> channels维护，根据这些Socket，ProjectMain将每一Socket对应的ObjectOutputStream保存在一张哈希表（HashMap<Integer,ObjectOutputStream> outputStreamHashMap）中，用于发消息。
  邻节点序号被保存在int[] neighbors中，所有邻节点和信道都通过其节点序号唯一标识。
- 系统拓扑
  整个系统先由Map类开始，Map类通过构造方法输入整个系统的拓扑，然后生成进程类ProjectMain类的实例并向该节点输入拓扑，该拓扑包含三个部分：
  - ArrayList<Node> nodesInSystem
    Node类保存一个节点在分布式系统中的标识——节点号、地址、端口。Map类通过一个无序ArrayList保存所有的Node实例，即所有进程的唯一标识和通信地址。
  - int [][] adjMatrix
    拓扑信息保存在这个最简易的邻接矩阵中——0表示不通，1表示通。进程类ProjectMain在开始初始化时会根据自身节点号和这一邻接矩阵找出所有与自己相邻的进程（节点）并建立Socket连接。
  - int currentNode
    对应该进程节点在ArrayList nodesInSystem中的序号，与节点序号不能划等号，因为节点序号存储在进程对应的Node实例中，会在初始化阶段读取。
  Map类输入拓扑信息的方式不限，可以通过文件输入也可以直接写入代码，甚至可以通过一个专门的监管节点通过消息注入。
  进程初始化的过程参考ProjectMain类中main函数，本进程(mainObj)首先根据currentNode和nodesInSystem确定自身节点信息。然后将nodesInSystem中所有节点信息根据节点序号装入一张HashMap中，方便调用。
  然后mainObj根据邻接矩阵建立Socket连接并设定监听端口，开始监听。
- 线程规划
  系统简化为三个线程。
  - EmitMessageThread
    发信线程负责令mainObj开放通信窗口，然后mainObj在每一窗口内生成随机数作为发消息的数量，具体细节见ProjectMain类中的 emitMessages 方法。
    在每一次发消息过程中，首先通过mainObj中的状态变量State判断此时节点是否在快照。
    如果在快照过程中，为了保持节点状态不变，mainObj在快照过程中产生的所有业务消息(ApplicationMsg实例)会被存储在一个HashMap<Integer,ConcurrentLinkedQueue> waitingBuffer中，waitingBuffer中每一个键值对对应一条信道(Channel)上等待消息的队列，
    其中，键是邻接信道序号，所有邻接信道的Socket实例作为值与这些序号一一对应存储在HashMap<Integer,Socket> channels中。
    待节点状态变为working，一一发送这些waitingBuffer中的消息，并修改金库信息。
  - ClientThread
    监听线程负责从相应端口收取消息，并在判断消息类型后作出反应。
    所有消息继承自消息类Message，均为可序列化类，因此Socket通信过程中可以直接作为对象传输。
    而监听线程受到对象后也可以通过运算符instanceof判断Message类型。目前系统使用ApplicationMsg与MarkerMsg两种消息，分别为业务消息和分隔符消息。为了实现快照算法，预计还需要一种用于触发快照的消息和终止快照的终结消息，不过这一部分因快照算法的设计而异。
  - ChandyLamportThread
    这一线程就是需要设计的ChandyLamport算法，原算法没有具体实施细则，因此需要自行设计快照的触发条件、终止条件等。
