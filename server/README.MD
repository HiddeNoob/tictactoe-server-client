# Tic-tac-toe Server
- genel olarak yapıyı şu şekilde düşündüm.
- kullanıcılar servera websocket üzerinden bağlanır. Her bağlantıda yeni bir thread oluşur
- kullanıcılar bir bağlantıya sahiptir.
- kullanıcılar lobi kurar.
- her bir lobide 1 Game instance'ı bulunur.
- her bir Game de 1 Map bulunur
#### Bu yüzden yapı;
>
> - *User has WebsocketConnection*
> - *Lobby has Game*
> - *Game has Map*

client ile server arasındaki haberleşmeyi verimli hale getirmek için Google'ın sağladığı
[protobuf](https://protobuf.dev/)'ı kullandım.

client ile haberleşmeyi [websocket standartlarına](https://datatracker.ietf.org/doc/html/rfc6455#section-5.2) dayanarak sıfırdan kodladım.


## Nasıl çalıştırılır?
- Maven 3.9.10 ile test edildi
```
mvn compile
mvn exec:java
```