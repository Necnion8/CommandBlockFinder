# CommandBlockFinder
ワールドに存在するコマンドブロックを探します<br>
アンロードされているチャンクからの検索はできませんが、その分検索の待ち時間はありません。

[YouTube](https://youtu.be/Tde872EL28I)

## 前提
- Spigot 1.14 以上

## コマンドと権限
- コマンドブロック検索杖を与えます - `/wandcommandblock`
> 権限: `commandblockfinder.admin` (default: OP)<br>

- コマンドブロックの検索 - `/findcommandblock`
> 権限: `commandblockfinder.admin` (default: OP)<br>
> 引数:<br>
> - 距離を指定するオプション `r:(距離)`<br>
> - ワールドを指定するオプション `w:(ワールド名)`<br>
> - 設定されているコマンド文字列の一部を残りの引数で与えられます<br>
>
> 例: `/findcommandblock r:10 tellraw`<br>
> 実行場所から10ブロック以内のコマンドブロックかつ、設定コマンドに tellraw が含まれるものを検索
