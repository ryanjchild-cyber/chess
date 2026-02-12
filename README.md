# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.
This link is to the design of the server:
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygRUQHPCMykoKp+h-Ds0KPHCFRvl2MAIEJ4oYoJwkEkSYDTuWKkwGpOzEqSlkVMO-K1IyLJESgrnvEqc40vuFTLmKEpujKcpluFcyYCqwYam6RocBAahoAA5MwVpojA4pGNoegGDeUUjkm5SWbUCVbr5FnVM8ABCIZOWoYBRjGcaFFpFSibUACseEETmqh5vM0FFiW9T6TAaAQMwABmvicA29HVbytWtZ2jUzlu6XqjAACSaAgNAKLgDATRGep24oHVOl1A54oiuCKAMW9dWgbUD1bDsfUoLGCnoVAdUjeN+GjGMU0zQWYzzdA9TitdxYoHdnkAttTYteUb21B9aAZKoAEYk18ikn9B01LU4FufMmltVDw3IKmuFw0zKXzCRqHfBRVH1gLtGNgxnjeH4-heCg6AxHEiRywrDm+FgomCgDjTSBG-ERu0EbdD0cmqApwzC4h6CQ9pZnPJbSGYH99XOl2Nn2Or9lCer3U5Oel4i2g3nPX5t4BSA8TY2qABqJpIBwgf+-BVtB7tC6qDFwrMtgWjojApsKTABA-UXECl47xh1f59JwAUa1IFAMwKjnchiNXGf00iaunuTAEh7bsLPN3YC9wggF2+zLsSe93s9-+Y82xzJRgLU3PDGMDmMuu6v5z4+5rb2CCWGMEuMdLAQouu-jYOKGr8SVADiSoaJr4kM40D8G8b9hKhbAcpzbImE9agO2ts7BqyA-b3xyL7FyoCg4+VdqHGqAUgqJ3gZFPa-JM61DgJHEAaoBRIDWsaf+SFSoCioHHBildkFYJrnXBuTd4Fp2ip3F0tc0D10bqQ5OjtToangRQ5ah9+5TwZs+eI0AkAAC88gFFRlARe5QRrpgAIyTX5EjOaxY0YKmmJeaRMifr4wYr5co7daiQLAIGBAT8cwYkwenHBFBuB3R-nMGAa1vBN1zrofQYhaEWLDvSFEYAfDvBgB4lA0CwANE6Kw-a4ikRhIiWgKJSpYnxLEcTVJkTolZISXTcRzw8npIKWibJQ0VGc2wmveGiN8wMkyZUzodEmxS2Yv4DgAB2NwTgUBOBiBGYIcAuIADZ4ATkMPYwwRRaliV9GzeoUkOjf1-gYvh6AszRIAHJKlZtPAeX4QFkPQGsEYeyDlOwnuwqyh5W6zKTpRFOKwrlzEQZ2Oh6dTl6iMaFDBiTsHlFiuKdckByFlT8ZVQJOgq4hNHJw7hzCzmFHbv9V2uDGE8JYeYyxDz0RPPeVyIF6gcEAFlwzLRQJsDJnj47mGIagJRQTLFlLpYYBlGAmW0HRXc2o7LomlQ4Iy+utBCa5KZGkjlwrRXMpuYPSeANBUVi5QQMVSjqkjXqdmLRTSZVqp5VAdpksmIy0sCgPsEBaWxCQAkMAFqrW0oAFIQDKrMmIyRQBqnmcvLWyzGhNGZDJHo0S-5bMUqMbACBgAWqgLXGyUA1jRPOtIQ5NRjlwl+S8pCqkY1xoTdAZNSpU0Kq-PygAVm6smKBCQ9QxFW8UTz4F5tjZQQtSaOWpuDnihF2b-nPMDk46KILhRxXBSnUq6ToUBM0HC75+4sVcKYbwnN1s+XJI4dilFEa0qqg1J1dcsCxEA0PbAxRyjVFOA0Q0vVs1Cy6NLHodcKI605FMSHYJKD6RBSJSW6Qw7aqjtFGCrt0gp3lXkDCud8Lv2jhVXMUtG6GoIZQKWgRMAPW7NWjAEUm0zFIPpv6WZ2GwB4Z8JwUG4N4xaoWWmJwgRNG5n1SjR9zTPErXWvhj9GHzokKaPmjC50BQAHV4i6jEL2uDtRNjifRAJttUAMQpukGsfSgHgXLj8LnQwsmJMeVQHSGDC6RxLuRTAGYEAaCko7pursSKV2WesxK4BDmm5ObEMUgGDmeEecvXR7mp9eP8cE7AYTMB2oxoIZ+yxXqCEKbjcp-9amQoabJcB7TrdZRRY1DfbGahjNfvoYi7dFmrNtwRRiw6Pn3PlZya5xhtXrNeeWTVsrzm2bQwC3hU+O1Okyy8LGxWdrlZQCG4gYMsBgDYGjYQeR6TfXmH9dPFZut9aG2NsYQBxMQDcDwLAjEu3JuONprcuzY49tKc+VQUQJnw6XfLugVQKXKDPY5baSrwGRXcDzigGolg9TikcGVPUhgZ1VVZX2tr4POUCiOLGSwNmqs3a3cunhMPhHw4dfVxVZmkC+IqgEzHMwEdlvMkRuo0PCcGGJ6T2jy9V49Z2kAA

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
