# 闈掑敭 路 鏍″洯浜屾墜浜ゆ槗骞冲彴

涓€涓潰鍚戞牎鍥殑浜屾墜浜ゆ槗骞冲彴锛歋pring Boot 3 + MySQL 8 + Redis锛屽墠绔负鍗曟枃浠?Vue3 + Element Plus銆?
## 鎶€鏈爤

| 灞?| 鎶€鏈?|
|---|---|
| 鍚庣 | Spring Boot 3.5.16銆丮yBatis-Plus 3.5.18銆丣WT锛坖jwt 0.12锛夈€丅Crypt |
| 鏁版嵁搴?| MySQL 8.0锛圖ocker锛夈€丷edis 7锛圖ocker锛?|
| 閭欢 | QQ 閭 SMTP锛坰mtp.qq.com:465 + SSL锛夛紝鐢ㄤ簬瀛︽牎閭楠岃瘉鐮佹敞鍐?|
| 鍓嶇 | Vue 3 + Element Plus锛圕DN 寮曞叆锛屽崟鏂囦欢 `frontend/index.html`锛?|
| AI | DeepSeek Chat Completions锛堝彲閫夛紝鏈厤缃?Key 鏃惰繑鍥炴ā鎷熸枃妗堬級 |

## 鐩綍缁撴瀯

```
campus-market/
鈹溾攢鈹€ backend/                     Spring Boot 鍚庣
鈹?  鈹溾攢鈹€ src/main/java/com/campus/market/
鈹?  鈹?  鈹溾攢鈹€ user/                鐢ㄦ埛锛氭敞鍐屻€佺櫥褰曘€佷釜浜轰腑蹇?鈹?  鈹?  鈹溾攢鈹€ item/                鍟嗗搧锛氬彂甯冦€佸垪琛ㄦ悳绱€佽鎯?鈹?  鈹?  鈹溾攢鈹€ order/               璁㈠崟 + 瓒呮椂鍙栨秷瀹氭椂浠诲姟
鈹?  鈹?  鈹溾攢鈹€ review/              璇勪环锛堜拱瀹惰瘎鍗栧锛岃仈鍔ㄤ俊鐢ㄥ垎锛?鈹?  鈹?  鈹溾攢鈹€ topup/               缃《璐拱锛堢敵璇?瀹℃牳锛?鈹?  鈹?  鈹溾攢鈹€ admin/               绠＄悊鍛橈細缃《銆佸皝绂?鈹?  鈹?  鈹溾攢鈹€ ai/                  DeepSeek 鏂囨鐢熸垚
鈹?  鈹?  鈹溾攢鈹€ file/                鍥剧墖涓婁紶
鈹?  鈹?  鈹溾攢鈹€ security/            JWT 鎷︽埅鍣ㄣ€佷笂涓嬫枃
鈹?  鈹?  鈹斺攢鈹€ common/              缁熶竴杩斿洖銆佸紓甯稿鐞嗐€佹晱鎰熻瘝
鈹?  鈹斺攢鈹€ src/main/resources/application.yml
鈹溾攢鈹€ frontend-app/                Vite + Vue3 鍓嶇宸ョ▼锛堝敮涓€鍓嶇锛?鈹?  鈹溾攢鈹€ src/
鈹?  鈹?  鈹溾攢鈹€ components/          缁勪欢锛欰ppShell / ItemCard / 寮圭獥绛?鈹?  鈹?  鈹溾攢鈹€ views/               椤甸潰锛歀ogin / Home / Profile / Publish / Detail / Admin
鈹?  鈹?  鈹溾攢鈹€ router/              Vue Router + 閴存潈瀹堝崼
鈹?  鈹?  鈹溾攢鈹€ stores/              Pinia锛堢敤鎴?Token / 璧勬枡锛?鈹?  鈹?  鈹溾攢鈹€ api/                 fetch 灏佽锛坱oken 娉ㄥ叆銆?01 澶勭悊锛?鈹?  鈹?  鈹溾攢鈹€ constants/           鐢ㄦ埛鍗忚 / 闅愮鏀跨瓥 / 鍏嶈矗澹版槑鏂囨湰
鈹?  鈹?  鈹斺攢鈹€ styles/              鍏ㄥ眬涓婚鏍峰紡
鈹?  鈹溾攢鈹€ vite.config.js           dev 绔彛 5174 + /api銆?uploads銆?ws 浠ｇ悊
鈹?  鈹斺攢鈹€ README.md                鍓嶇宸ョ▼杩愯璇存槑
鈹溾攢鈹€ sql/                         澧為噺杩佺Щ鑴氭湰锛?02 ~ 012锛?鈹溾攢鈹€ docs/                        鏂囨。涓庢埅鍥?鈹溾攢鈹€ init.sql                     鍏ㄩ噺寤哄簱寤鸿〃鑴氭湰
鈹溾攢鈹€ docker-compose.yml           MySQL + Redis
鈹溾攢鈹€ start-backend.cmd / .ps1     鍚姩鍚庣
鈹溾攢鈹€ stop-backend.cmd / .ps1      鍋滄鍚庣锛堥噴鏀?8080锛?鈹溾攢鈹€ start-frontend.cmd           鍚姩 Vite 鍓嶇锛?174锛?鈹溾攢鈹€ build-backend.cmd            鎵撳寘鍚庣 jar
鈹溾攢鈹€ .env.local                   鏈湴瀵嗛挜锛堝凡 gitignore锛屼笉杩涚増鏈簱锛?鈹斺攢鈹€ .tools/                      渚挎惡 JDK17 + Maven锛堝凡 gitignore锛?```

## 蹇€熷惎鍔紙姣忔寮€鍙戠殑鏍囧噯姝ラ锛?
1. **鍚姩 Docker Desktop**锛岀瓑鐘舵€佸彉鎴?Running
2. 鍦ㄩ」鐩牴鐩綍鍚姩渚濊禆瀹瑰櫒锛?   ```powershell
   cd D:\campus-market
   docker compose up -d
   docker compose ps        # mysql / redis 涓や釜瀹瑰櫒閮藉簲涓?healthy
   ```
   > 棣栨鍒濆鍖栦細鑷姩鎵ц `init.sql`锛涘凡鏈夋暟鎹簱鍗囩骇鏃讹紝鎸夐『搴忔墽琛?`sql/002 ~ 014` 鐨勫閲忚剼鏈€?3. **鍚姩鍚庣**锛氬弻鍑?`start-backend.cmd`
   - 鎴愬姛鏍囧織锛氭棩蹇楀嚭鐜?`Tomcat started on port 8080` 鍜?`Started CampusMarketApplication`
   - 鍚庣鍦板潃锛歨ttp://localhost:8080
4. **鍚姩鍓嶇**锛氬弻鍑?`start-frontend.cmd`锛圴ite 宸ョ▼锛宧ttp://localhost:5174锛?   - 棣栨杩愯浼氳嚜鍔?`npm install`锛堥渶瑕?Node.js 18+锛?   - 涔熷彲鎵嬪姩锛歚cd frontend-app && npm run dev`
5. **鍋滄**锛氬弻鍑?`stop-backend.cmd` 鍋滄鍚庣锛堥噴鏀?8080锛夛紝鍓嶇鍦ㄥ懡浠よ Ctrl+C 鍋滄锛堥噴鏀?5174锛?
> 鍚庣鏃ュ織鍦?`logs\startup-*.log`锛屽惎鍔ㄥけ璐ユ椂鎶婃渶鍚?30 琛屽彂鍑烘潵鍗冲彲瀹氫綅銆?> `.env.local` 淇濆瓨鏈満瀵嗛挜锛歚JWT_SECRET`銆丏eepSeek Key銆侀偖绠辨巿鏉冪爜锛涘惎鍔ㄨ剼鏈細鑷姩鍔犺浇锛屼笉浼氳繘 git銆?> 鏃х増 `frontend/*.html` 宸插垹闄わ紝鍙淮鎶?`frontend-app` 涓€濂椾唬鐮併€?
## 娴嬭瘯璐﹀彿

| 瑙掕壊 | 璐﹀彿 | 瀵嗙爜 | 璇存槑 |
|---|---|---|---|
| 鍗栧 | alice01 | 123456 | 宸插彂甯?4 浠跺晢鍝侊紝淇＄敤鍒?120锛堟紨绀洪珮淇¤獕鏍囩锛?|
| 涔板 | buyer01 | 123456 | 鐢ㄤ簬涓嬪崟 / 瀹屾垚浜ゆ槗 |
| 涔板 | buyer02 | 123456 | 鐢ㄤ簬娴嬪苟鍙?鎶㈠崟 |
| 绠＄悊鍛?| admin01 | 123456 | role=2锛屽敮涓€绠＄悊鍛橈紱鍙疆椤跺鏍搞€佸皝绂佺敤鎴?|

> 瑙掕壊瀛楁 `user.role`锛?*1=鏅€氱敤鎴凤紝2=绠＄悊鍛?*銆傚彧鏈?admin01 鑳借繘鍏ョ鐞嗗悗鍙般€?> 娉ㄥ唽鏂拌处鍙烽渶瑕?`.edu.cn` 瀛︽牎閭 + 閭楠岃瘉鐮併€?
## 椤甸潰鍦板潃

鏂扮増 Vite 宸ョ▼锛堟帹鑽愶紝绔彛 5174锛夛細

| 椤甸潰 | 鍦板潃 | 璇存槑 |
|---|---|---|
| 鐧诲綍 / 娉ㄥ唽 | http://localhost:5174/login | 鏈櫥褰曡闂叾浠栭〉闈細鑷姩璺宠繖閲?|
| 棣栭〉 | http://localhost:5174/ | 鎼滅储銆佹牎鍖虹瓫閫夈€侀鏋跺睆銆佹棤闄愭粴鍔?|
| 娑堟伅 | http://localhost:5174/messages | 妯℃嫙閫氱煡 |
| 鎴戠殑 | http://localhost:5174/profile | 鎴戝彂甯冪殑 / 鎴戜拱鍒扮殑 / 鎴戝崠鍑虹殑 |
| 鍙戝竷闂茬疆 | http://localhost:5174/publish | 闇€鐧诲綍 |
| 鍟嗗搧璇︽儏 | http://localhost:5174/detail/3 | `3` 涓哄晢鍝?ID |
| 绠＄悊鍚庡彴 | http://localhost:5174/admin | 浠?admin01 鍙繘 |

鏃х増闈欐€侀〉宸插垹闄わ紝鍙繚鐣?`frontend-app` 涓€濂楀墠绔唬鐮併€?
## 鍓嶇宸ョ▼鍖栬鏄庯紙Vite + Vue3锛?
- **缁勪欢鎷嗗垎**锛歚AppShell`锛堜晶杈规爮/搴曢儴瀵艰埅锛夈€乣ItemCard`銆乣SkeletonGrid`銆乣TopUpDialog`銆乣EditItemDialog`銆乣ReviewDialog`
- **璺敱**锛歏ue Router锛坄/login`銆乣/`銆乣/messages`銆乣/profile`銆乣/publish`銆乣/detail/:id`銆乣/admin`锛夛紝甯︾櫥褰?绠＄悊鍛樺畧鍗?- **鐘舵€?*锛歅inia `useUserStore` 绠＄悊 token銆佺敤鎴疯祫鏂欍€佺櫥褰曠櫥鍑猴紝骞舵寔涔呭寲鍒?localStorage
- **鎺ュ彛灞?*锛歚src/api/http.js` 缁熶竴娉ㄥ叆 token锛?01 鑷姩娓呯悊鐧诲綍鎬佸苟璺宠浆鐧诲綍椤?- **寮€鍙戜唬鐞?*锛歏ite 鎶?`/api`銆乣/uploads`銆乣/ws` 浠ｇ悊鍒?`http://localhost:8080`
- 璇︾粏鍛戒护涓庣洰褰曡 `frontend-app/README.md`

## 瀹夊叏鍔犲浐锛堜笂绾垮墠 P0 淇锛?
| 椤?| 瀹炵幇 |
|---|---|
| 閭鍞竴 | `user.school_email` 鍞竴绱㈠紩锛堣縼绉?010锛夛紱娉ㄥ唽鍓嶆鏌ラ偖绠辨槸鍚﹀凡娉ㄥ唽 |
| 閲嶇疆瀵嗙爜闃叉帴绠?| 蹇呴』鍚屾椂鍖归厤銆岀敤鎴峰悕 + 閭 + 楠岃瘉鐮併€嶏紝缂轰竴涓嶅彲 |
| 鍙屽悜纭浜ゆ槗 | `0 寰呴潰浜?鈫?4 寰呭崠瀹剁‘璁ゅ畬鎴?鈫?1 宸插畬鎴恅锛涗拱瀹剁‘璁ゅ彧鏀硅鍗曠姸鎬侊紝**鍗栧纭鍚?*鎵嶅敭鍑哄晢鍝併€佸弻鏂?+1 淇＄敤鍒?|
| 鑱旂郴鏂瑰紡闃茬埇 | 璇︽儏鎺ュ彛鍙繑鍥炴帺鐮侊紙濡?`wx: xia***01`锛夛紱瀹屾暣鍊艰蛋 `GET /api/item/{id}/contact`锛宍@RateLimit(60s, 3娆?`锛岀偣鍑诲鍒舵墠璇锋眰 |
| 涓婁紶閰嶉 | 姣忕敤鎴锋渶澶?50 寮狅紙`file_upload` 琛ㄧ粺璁★紝瓒呴檺 400锛?|
| CORS 鐧藉悕鍗?| 榛樿鍙厑璁?`http://localhost:5174` / `127.0.0.1:5174`锛岀敓浜х敤 `CORS_ALLOWED_ORIGINS` 瑕嗙洊 |
| JWT 瀵嗛挜 | 鍙粠鐜鍙橀噺 `JWT_SECRET` 璇诲彇锛堝啓鍦?`.env.local`锛岃嚦灏?32 瀛楃锛夛紝yml 涓嶅啀纭紪鐮?|
| 绉诲姩绔敭鐩?| 杈撳叆妗嗚仛鐒﹁嚜鍔?`scrollIntoView`锛宍scroll-padding-bottom` 棰勭暀閿洏绌洪棿 |
| 楠ㄦ灦灞?| 棣栭〉銆佹垜鐨勩€佺鐞嗗悗鍙拌〃鏍煎潎鏈夐鏋跺睆 |
| 涓炬姤涓庝笅鏋?| `report` 琛?+ 璇︽儏椤典妇鎶ュ叆鍙?+ 绠＄悊鍚庡彴銆屼妇鎶ュ鐞嗐€峊ab锛屼竴閿笅鏋惰繚瑙勫晢鍝?|
| 璁㈠崟闃插崰鍗?| 姣忎拱瀹舵渶澶?3 绗旇繘琛屼腑璁㈠崟锛涗拱瀹跺彲鍙栨秷锛涘緟闈氦 3 澶╄嚜鍔ㄥ彇娑?|
| 绠＄悊鍛樺璁?| 缃《/灏佺/涓嬫灦绛夋搷浣滃啓鍏?`admin_audit_log` |
| 鏁忔劅璇嶅缃?| 璇嶅簱杩佸埌 `application.yml` 鐨?`app.sensitive-words` |

## 鍚堣涓庢硶寰嬫枃鏈?
- 娉ㄥ唽椤垫柊澧?**鍚屾剰銆婄敤鎴峰崗璁€?銆婇殣绉佹斂绛栥€?* 鍕鹃€夋锛屾湭鍕鹃€変笉鍏佽娉ㄥ唽
- 鍗忚涓庨殣绉佹斂绛栫畝鏄撴枃鏈湪 `frontend-app/src/constants/legal.js`锛屽脊绐楀彲婊氬姩闃呰
- 鍏ㄧ珯椤佃剼灞曠ず鍏嶈矗澹版槑锛氬钩鍙颁粎鎻愪緵淇℃伅鎾悎锛岀嚎涓嬩氦鏄撻闄╄嚜璐?- 涓婄嚎鍓嶄粛闇€锛欼CP/鍏畨澶囨銆佸浘鐗囧唴瀹瑰鏍搞€佷妇鎶ョ敵璇夋祦绋嬪畬鍠勶紙璇﹁涓婄嚎璇勪及鎶ュ憡 P0 娓呭崟锛?
## 鏁版嵁鐘舵€?
浠撳簱榛樿鏁版嵁搴撲负骞插噣鐨勬紨绀烘暟鎹細4 浠跺湪鍞晢鍝侊紙item 3 缃《涓級銆? 涓祴璇曡处鍙枫€? 璁㈠崟銆? 缃《鐢宠銆? 璇勪环銆?鎵€鏈変笟鍔℃祦绋嬶紙娉ㄥ唽 鈫?鍙戝竷 鈫?娴忚 鈫?涓嬪崟 鈫?鍗栧纭 鈫?涔板纭闈氦 鈫?鍗栧纭瀹屾垚/鎷掔粷 鈫?璇勪环 鈫?缃《鐢宠 鈫?绠＄悊鍛樺鏍革級鍧囧彲鐩存帴鐢ㄤ笂杩拌处鍙疯窇閫氥€?
## Redis 鍟嗗搧鍒楄〃缂撳瓨

棣栭〉 `/api/item/list` 宸叉帴鍏?Redis 缂撳瓨锛? 鏍?4G 閮ㄧ讲涓嶅紩鍏?Elasticsearch锛屾悳绱粛璧?MySQL锛夛細

- **缂撳瓨 key**锛歚qingshou:item:list:{md5(鐗堟湰鍙穦keyword|category|campus|sort|page|size)}`
- **TTL**锛氭甯哥粨鏋?5 鍒嗛挓 + 闅忔満 0~60 绉掞紙闃查洩宕╋級锛涚┖缁撴灉鍥哄畾 60 绉掞紙闃茬┛閫忥級
- **澶辨晥绛栫暐**锛氬彂甯冦€佺紪杈戙€佷笅鏋躲€佷笅鍗曘€佸畬鎴愪氦鏄撱€佸崠瀹舵嫆鍗曘€佽秴鏃跺彇娑堛€佺鐞嗗憳缃《/鍙栨秷缃《銆佺疆椤跺鏍搁€氳繃绛夊啓鎿嶄綔閮戒細鎵ц `INCR qingshou:item:list:version`锛屾墍鏈夋棫 key 绔嬪嵆閫昏緫澶辨晥
- **闄嶇骇**锛歊edis 寮傚父鏃惰嚜鍔ㄥ洖閫€ MySQL 鏌ヨ锛屾帴鍙ｄ笉鎶ラ敊

瀹炴祴缁撴灉锛氶娆¤姹傚洖婧愬苟鐢熸垚缂撳瓨锛圱TL鈮?46s锛夛紝浜屾璇锋眰鍛戒腑锛涚┖缁撴灉缂撳瓨 TTL鈮?9s锛涚紪杈戝晢鍝佸悗鐗堟湰鍙疯嚜澧炪€佸垪琛ㄧ珛鍗宠繑鍥炴柊浠锋牸銆?
## WebSocket 璁㈠崟瀹炴椂閫氱煡

- **杩炴帴鍦板潃**锛歚ws://localhost:8080/ws?token={JWT}`锛堟祻瑙堝櫒 WebSocket 鏃犳硶甯?Header锛宼oken 鏀炬煡璇㈠弬鏁帮級
- **鎻℃墜閴存潈**锛氭牎楠?JWT銆佺敤鎴峰瓨鍦ㄤ笖鏈灏佺锛涙棤 token / 杩囨湡鐩存帴 401
- **浼氳瘽绠＄悊**锛氭寜 userId 缁存姢杩炴帴闆嗗悎锛屽悓涓€璐﹀彿澶氱鍦ㄧ嚎閮戒細鏀跺埌锛涘績璺?`ping/pong`
- **鎺ㄩ€佹椂鏈?*锛氫拱瀹朵笅鍗曟垚鍔熷悗锛岄€氳繃 `@TransactionalEventListener(AFTER_COMMIT)` 鍦ㄤ簨鍔℃彁浜ゅ悗鎺ㄩ€侊紝璁㈠崟鍥炴粴涓嶄細璇姤
- **娑堟伅浣?*锛?  ```json
  {"type":"NEW_ORDER","title":"闈掑敭鎻愮ず","message":"鏈夋柊璁㈠崟鍟︼紝璇峰強鏃剁‘璁わ紒","orderId":20,"itemId":3,"itemTitle":"iPad 绗?9 浠?64G"}
  ```
- **鍓嶇琛屼负**锛歚frontend-app` 鐧诲綍鍚庤嚜鍔ㄨ繛鎺ワ紝鏂嚎 5 绉掗噸杩烇紝閫€鍑虹櫥褰曡嚜鍔ㄥ叧闂紱鏀跺埌鍚?`ElNotification` 寮瑰嚭銆屾湁鏂拌鍗曞暒锛岃鍙婃椂纭锛併€嶃€?- **鐢熶骇閮ㄧ讲**锛歂ginx 闇€瑕佽浆鍙?WebSocket 鍗囩骇锛?  ```nginx
  location /ws {
      proxy_pass http://127.0.0.1:8080;
      proxy_http_version 1.1;
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection "upgrade";
      proxy_read_timeout 3600s;
  }
  ```

> 瀹炴祴锛歛lice01 鐧诲綍鍚庡缓绔嬭繛鎺ワ紙鍚庣鏃ュ織 `WebSocket 杩炴帴寤虹珛: userId=3`锛夛紝buyer01 涓嬪崟鍚庡墠绔敹鍒板苟寮瑰嚭銆岄潚鍞彁绀?/ 鏈夋柊璁㈠崟鍟︼紝璇峰強鏃剁‘璁わ紒銆嶃€?
## 鎺ュ彛闄愭祦 @RateLimit

鑷畾涔夋敞瑙?+ AOP + Redis 瀹炵幇鍥哄畾绐楀彛闄愭祦锛岄槻姝㈠埛鎺ュ彛锛?
```java
@RateLimit(time = 60, count = 5)   // 60 绉掑唴鏈€澶?5 娆?@PostMapping
public Result<?> publish(...) { ... }
```

- **瀹炵幇**锛歚RateLimitAspect` 鐢?Lua 鑴氭湰鎵ц `INCR` + 棣栨 `EXPIRE`锛堝師瀛愭搷浣滐級锛宬ey 涓?  `qingshou:rate:{Controller}.{method}:{鐢ㄦ埛ID 鎴?IP}`
- **闄愭祦缁村害**锛氬凡鐧诲綍鎸?`userId`锛屾湭鐧诲綍鎸夊鎴风 IP锛堟敮鎸?`X-Forwarded-For`锛?- **瓒呴檺鍝嶅簲**锛欻TTP/涓氬姟鐮?429锛屾彁绀恒€屾墜閫熷お蹇暒锛岃绋嶅悗鍐嶈瘯銆?- **闄嶇骇**锛歊edis 寮傚父鏃舵斁琛岋紙fail-open锛夛紝閬垮厤缂撳瓨鏁呴殰鎷栧灝涓氬姟
- **宸叉帴鍏?*锛歚POST /api/item`锛堝彂甯冨晢鍝侊級銆乣POST /api/user/send-code`锛堝彂閫侀獙璇佺爜锛夈€乣POST /api/report`锛堜妇鎶ワ級銆乣GET /api/item/{id}/contact`锛堣仈绯绘柟寮忥紝60 绉?3 娆★級

瀹炴祴锛氳繛缁 6 娆″彂甯?鍙戠爜鍧囪繑鍥?`code=429, message=鎵嬮€熷お蹇暒锛岃绋嶅悗鍐嶈瘯`锛汻edis 涓搴?key `count=6`銆乀TL 姝ｅ父閫掑噺銆?
## 涓炬姤涓庡悗鍙板鐞?
- 鍟嗗搧璇︽儏椤靛彸涓嬭鏈変綆璋冪殑銆屼妇鎶ヨ鍟嗗搧銆嶅叆鍙ｏ紝寮圭獥閫夋嫨鍘熷洜锛堣繚绂佸搧/璇堥獥/杈遍獋/渚垫潈/鍏朵粬锛? 琛ュ厖璇存槑
- 鍚屼竴涓炬姤浜哄鍚屼竴瀵硅薄鍙兘鏈変竴鏉″緟澶勭悊涓炬姤
- 绠＄悊鍚庡彴鏂板銆屼妇鎶ュ鐞嗐€峊ab锛氬睍绀哄晢鍝併€佷妇鎶ヤ汉銆佸師鍥犮€佹椂闂达紝鍙竴閿€屼笅鏋跺晢鍝併€嶆垨銆屽拷鐣ャ€?- 涓嬫灦浼氭妸鍟嗗搧缃负 `status=3`銆佸彇娑堢疆椤讹紝骞跺啓绠＄悊鍛樺璁℃棩蹇?- 琛ㄧ粨鏋勶細`report`锛堣縼绉?013锛夛紝瀛楁鍚?`item_id / target_user_id / reporter_id / reason / status / handler_id / handle_time`

瀹炴祴锛歜uyer01 涓炬姤 item5 鈫?绠＄悊鍚庡彴鍑虹幇 1 鏉″緟澶勭悊 鈫?涓嬫灦鍚?item5 `status=3`銆佷妇鎶?`status=1`銆佸璁℃棩蹇?`DELIST_ITEM`銆?
## 璁㈠崟闃插崰鍗曚笌瓒呮椂

- 鍚屼竴涔板鏈€澶?**3 绗?*杩涜涓鍗曪紙鐘舵€?3/0/4锛夛紝绗?4 绗旂洿鎺?400
- 涔板鍙富鍔ㄥ彇娑堛€屽緟鍗栧纭 / 寰呴潰浜ゃ€嶈鍗曪紝鍟嗗搧绔嬪嵆鎭㈠鍦ㄥ敭
- 瀹氭椂浠诲姟姣?10 鍒嗛挓鎵弿锛?  - 鐘舵€?3 瓒呰繃 24 灏忔椂 鈫?鑷姩鍙栨秷
  - 鐘舵€?0锛堝緟闈氦锛夎秴杩?**3 澶?*锛堟寜 `update_time`锛夆啋 鑷姩鍙栨秷骞舵仮澶嶅晢鍝佸湪鍞?- 杩愮淮鍙皟鐢?`POST /api/admin/maintenance/scan-timeout` 鎵嬪姩瑙﹀彂鎵弿

瀹炴祴锛? 鍗曞悗绗?4 鍗曡繑鍥炪€屾渶澶?3 绗斻€嶏紱鍙栨秷鍚庡晢鍝佹仮澶嶄笖鍙啀涓嬪崟锛涙妸寰呴潰浜よ鍗?`update_time` 鏀规垚 4 澶╁墠鍚庤Е鍙戞壂鎻忥紝璁㈠崟鍙樺凡鍙栨秷銆佸晢鍝佹仮澶嶅湪鍞€?
## 绠＄悊鍛樺璁℃棩蹇?
- 琛?`admin_audit_log`锛堣縼绉?014锛夛細`admin_id / action / target_id / detail / create_time`
- 宸茶褰曪細`TOP_ITEM`銆乣UNTOP_ITEM`銆乣BAN_USER`銆乣UNBAN_USER`銆乣DELIST_ITEM`銆乣DISMISS_REPORT`
- 鏌ヨ鎺ュ彛锛歚GET /api/admin/audit-logs?limit=100`

## 鏁忔劅璇嶅缃?
- 璇嶅簱鍦?`application.yml` 鐨?`app.sensitive-words`锛屾敼閰嶇疆鍗冲彲澧炲垹锛屾棤闇€鏀逛唬鐮?- 褰撳墠璇嶏細浠ｅ啓銆佽繚绂併€佽瘓楠椼€佹灙鏀€佹瘨鍝併€佸亣璇併€佸埛鍗曘€佷唬鑰?- 鍙戝竷/缂栬緫鍟嗗搧鏃跺懡涓洿鎺?400锛屾彁绀哄叿浣撴晱鎰熻瘝

瀹炴祴锛氭爣棰樺惈銆屼唬鍐欍€嶃€屽埛鍗曘€嶅潎琚?400 鎷掔粷锛屾彁绀哄搴旀晱鎰熻瘝銆?
## 绠＄悊鍚庡彴鏁版嵁鐪嬫澘

`GET /api/admin/stats`锛堜粎绠＄悊鍛橈級杩斿洖锛?
```json
{
  "totalUsers": 4,
  "todayNewUsers": 0,
  "onSaleItems": 4,
  "todayPublishedItems": 1,
  "completedAmount": 50.00,
  "trend": [{"date":"2026-11-08","label":"11-08","count":0}, "... 鍏?7 澶?]
}
```

- `completedAmount`锛氬凡瀹屾垚璁㈠崟锛坰tatus=1锛夊叧鑱?`item.price` 姹傚拰
- `trend`锛氳繎 7 澶╂瘡澶╁彂甯冪殑鍟嗗搧鏁帮紙鍚粖澶╋紝鏃ユ湡鍗囧簭锛?- 鍓嶇 `admin` 椤甸潰椤堕儴鐢?5 寮犲崱鐗囧睍绀猴紝鏁板瓧閫氳繃 `AnimatedNumber` 缁勪欢鍋氭粴鍔ㄥ姩鐢伙紙requestAnimationFrame + easeOutCubic锛?- 鏁版嵁鐪嬫澘涓嬫柟鐢?ECharts 缁樺埗杩?7 澶╁彂甯冭秼鍔挎姌绾垮浘锛堟笎鍙橀潰绉€佸钩婊戞洸绾裤€佽嚜閫傚簲 resize锛?- Vite 鐗?`AdminView.vue` 涓庢棫鐗?`frontend/admin.html` 鍧囧凡鎺ュ叆

瀹炴祴锛氫粖鏃ュ彂甯?1 浠躲€佸畬鎴?1 绗?50 鍏冭鍗曞悗锛屾帴鍙ｈ繑鍥?`todayPublishedItems=1`銆乣completedAmount=50.00`锛岃秼鍔垮浘涓?11-12 涓?4銆?1-14 涓?1銆?
## AI 鐢熸垚鍟嗗搧鏂囨

鍙戝竷椤碉紙Vite `PublishView.vue` 涓庢棫鐗?`publish.html`锛夊湪銆屽晢鍝佹弿杩般€嶅彸涓婅鎻愪緵 **鉁?AI甯垜鍐欐枃妗?* 鎸夐挳锛?
1. 鍙栥€愬晢鍝佹爣棰樸€?銆愬垎绫汇€戞嫾鎴愬叧閿瘝锛堟渶澶?50 瀛楋級
2. 璋冪敤 `POST /api/ai/generate-desc`锛堥渶鐧诲綍锛?3. 璇锋眰鏈熼棿鎸夐挳鏄剧ず銆屾鍦ㄧ敓鎴愪腑...銆嶅苟绂佺敤锛岄槻姝㈤噸澶嶇偣鍑?4. 杩斿洖鍚庤嚜鍔ㄥ～鍏ユ弿杩版锛屽苟鎻愮ず銆屽凡鏍规嵁鏍囬鍜屽垎绫荤敓鎴愭枃妗堛€?5. 鏈厤缃?`DEEPSEEK_API_KEY` 鏃跺悗绔繑鍥炪€屾ā鎷熸枃妗堛€嶏紝鍓嶇浼氭彁绀?mock 鐘舵€?6. 鏍囬涓虹┖鏃朵笉浼氬彂璇锋眰锛岀洿鎺ユ彁绀哄厛濉啓鏍囬

![AI 鏂囨](<docs/screenshots/publish-ai.png>)

## 鍏抽敭閰嶇疆

`backend/src/main/resources/application.yml`锛?
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: ${MAIL_USERNAME:}      # 鍙戜欢閭
    password: ${MAIL_AUTH_CODE:}     # QQ 閭 SMTP 鎺堟潈鐮侊紙涓嶆槸鐧诲綍瀵嗙爜锛?
deepseek:
  api-key: ${DEEPSEEK_API_KEY:}   # 浠庣幆澧冨彉閲忚鍙栵紝鍚姩鑴氭湰浼氬姞杞?.env.local
topup:                            # 闃舵瀹氫环锛?澶?鍏?/ 7澶?鍏?/ 30澶?2鍏冿紙鍚庣璁＄畻锛屼笉淇′换鍓嶇閲戦锛?order:
  timeout-hours: 24               # 寰呭崠瀹剁‘璁よ秴杩?24 灏忔椂鑷姩鍙栨秷
  timeout-cron: "0 */10 * * * *"  # 姣?10 鍒嗛挓鎵弿涓€娆?```

`.env.local`锛堥」鐩牴鐩綍锛屾湰鏈虹鏈夛紝宸插湪 .gitignore锛夛細
```
DEEPSEEK_API_KEY=sk-xxxx
MAIL_USERNAME=浣犵殑QQ閭@qq.com
MAIL_AUTH_CODE=浣犵殑SMTP鎺堟潈鐮?```

## 鎺ュ彛閫熸煡

| 妯″潡 | 鎺ュ彛 |
|---|---|
| 鐢ㄦ埛 | `POST /api/user/send-code`锛堝鏍￠偖绠遍獙璇佺爜锛夈€乣POST /api/user/register`锛堥渶 `emailCode`锛夈€乣POST /api/user/login`銆乣POST /api/user/send-reset-code`锛堝繕璁板瘑鐮佸彂鐮侊級銆乣POST /api/user/reset-password`锛堥獙璇佺爜閲嶇疆锛夈€乣GET /api/user/me` |
| 涓汉涓績 | `GET /api/user/me/items`銆乣GET /api/user/me/trades` |
| 鍟嗗搧 | `POST /api/item`銆乣GET /api/item/list?page=1&size=10&keyword=&category=&campus=&sort=`锛堣繑鍥?records + total锛夈€乣GET /api/item/{id}`銆乣PUT /api/item/{id}`锛堝崠瀹剁紪杈戯級銆乣POST /api/item/{id}/offline`锛堝崠瀹朵笅鏋讹級 |
| 璁㈠崟 | `POST /api/order`锛堟瘡涔板鏈€澶?3 绗旇繘琛屼腑锛夈€乣POST /api/order/{id}/seller-confirm`銆乣POST /api/order/{id}/complete`锛堜拱瀹剁‘璁ら潰浜わ級銆乣POST /api/order/{id}/seller-complete`锛堝崠瀹剁‘璁ゅ畬鎴愶級銆乣POST /api/order/{id}/buyer-cancel`銆乣POST /api/order/{id}/seller-reject`銆乣GET /api/order/buyer`銆乣GET /api/order/seller` |
| 璇勪环 | `POST /api/review`锛堣鍗曞畬鎴愬悗涔板璇勪环锛夈€乣GET /api/review/user/{userId}`锛堝尶鍚嶅彲鐪嬶紝璇︽儏椤靛睍绀猴級銆乣GET /api/review/mine`锛堟垜鍙戝嚭鐨勮瘎浠凤級 |
| 涓婁紶 | `POST /api/upload`锛坒orm-data锛屽瓧娈?`file`锛?|
| AI | `POST /api/ai/generate-desc` |
| 缃《鐢宠 | `POST /api/topup/apply`锛坆ody: `itemId` + `days`锛岄噾棰濆悗绔寜 1/7/30 澶╅樁姊绠楋級銆乣GET /api/topup/my`锛堝惈 `rejectReason`锛?|
| 涓炬姤 | `POST /api/report`锛坆ody锛歚itemId` 鎴?`userId` + `reason`锛? 鍒嗛挓 5 娆★級 |
| 绠＄悊 | `GET /api/admin/stats`锛堟暟鎹湅鏉匡級銆乣POST /api/admin/items/top`銆乣POST /api/admin/items/{id}/untop`銆乣POST /api/admin/users/{id}/ban`銆乣POST /api/admin/users/{id}/unban`銆乣GET /api/admin/topup/pending`銆乣POST /api/admin/topup/{id}/approve`銆乣POST /api/admin/topup/{id}/reject`锛坆ody 蹇呭～ `reason`锛夈€乣GET /api/admin/reports`銆乣POST /api/admin/reports/{id}/handle`锛坄delist` / `dismiss`锛夈€乣GET /api/admin/audit-logs`銆乣POST /api/admin/maintenance/scan-timeout` |

鎵€鏈夋帴鍙ｇ粺涓€杩斿洖 `{ "code": 200, "message": "...", "data": ... }`銆?
## 鍓嶇椤甸潰

| 椤甸潰 | 璇存槑 |
|---|---|
| `login.html` | 鐧诲綍 / 娉ㄥ唽 Tab锛涙敞鍐屽惈瀛︽牎閭楠岃瘉鐮侊紙60 绉掑€掕鏃讹級锛?*蹇樿瀵嗙爜**寮圭獥锛堥偖绠遍獙璇佺爜閲嶇疆锛?0 绉掑€掕鏃讹級锛涙垚鍔熷悗 token 瀛?localStorage 骞惰烦杞富搴旂敤 |
| `index.html` | 涓诲簲鐢紝鍝嶅簲寮忥細鎵嬫満搴曢儴瀵艰埅 + 鎮诞鍙戝竷锛屾闈㈠乏渚у浐瀹氳竟鏍忥紱涓婚〉鎼滅储 + 鏍″尯 Tab + **楠ㄦ灦灞?* + 鏃犻檺婊氬姩锛涘晢鍝佸崱鐗?馃敟 鎬ュ敭銆侌煆?楂樹俊瑾夛紱鎴戠殑锛堣祫鏂?/ 淇＄敤鍒?/ 鎴戝彂甯冪殑 **鍚紪杈戙€佷笅鏋?* / 鎴戜拱鍒扮殑 **鍚‘璁ゅ畬鎴愩€佽瘎浠峰崠瀹?* / 鎴戝崠鍑虹殑 **鍚悓鎰?鎷掔粷** / 閫€鍑虹櫥褰曪級锛?*鐐瑰嚮鍗＄墖璺宠浆 `detail.html?id=`** |
| `publish.html` | 鍙戝竷闂茬疆锛氭爣棰樸€佹弿杩般€佷环鏍笺€佸垎绫汇€佹牎鍖恒€佽仈绯绘柟寮?+ 鍥剧墖涓婁紶锛堣皟 `/api/upload`锛屽洖鏄鹃瑙堬級锛涙彁浜ゅ甫 token 璋?`/api/item`锛屾垚鍔熷悗寮圭獥骞惰繑鍥為椤?|
| `detail.html` | 鍟嗗搧璇︽儏锛氬ぇ鍥俱€佹爣棰樸€佷环鏍笺€佹弿杩般€佸崠瀹朵俊鎭紙馃弲 楂樹俊瑾夛級锛涘簳閮ㄣ€屾垜鎯宠銆嶆寜閽紝鐧诲綍鍚庡脊绐楀睍绀鸿仈绯绘柟寮忥紱澶嶅埗鎸夐挳鎸夊唴瀹瑰姩鎬佹樉绀恒€屽鍒禥Q鍙?/ 澶嶅埗寰俊鍙?/ 涓€閿鍒躲€嶏紱鏀寔寮圭獥鍐呫€岀洿鎺ヤ笅鍗曘€嶏紱**灞曠ず鍗栧璇勪环锛堝钩鍧囧垎 + 璇勪环鍒楄〃锛?* |
| `admin.html` | 绠＄悊鍛樺悗鍙帮紙浠?admin01锛夛細Tab 鍒囨崲銆岀疆椤跺鏍搞€嶏紙el-table 寰呭鏍哥敵璇凤細鍟嗗搧/鐢宠浜?澶╂暟/閲戦/鏃堕棿锛岀‘璁ゆ敹娆惧苟缃《 / 鎷掔粷锛変笌銆岀敤鎴峰皝绂併€嶏紙el-table 鍏ㄩ儴鐢ㄦ埛锛氳鑹?淇＄敤鍒?鐘舵€?el-tag锛屽皝绂?/ 瑙ｅ皝锛?|

鍓嶇閫氳繃 `http://localhost:8080` 鐩磋繛鍚庣锛孋ORS 宸插叏寮€鏀俱€傚缓璁敤 `start-frontend.cmd` 璧烽潤鎬佹湇鍔★紙`python -m http.server 5173`锛夛紝涓嶈鐩存帴鍙屽嚮 HTML銆?
## 瀛︽牎閭楠岃瘉鐮佹敞鍐?
1. `POST /api/user/send-code`锛宐ody `{"email":"瀛﹀彿@xxx.edu.cn"}`锛?   - 浠呮帴鍙?`.edu.cn` 缁撳熬閭
   - 鍚屼竴閭 1 鍒嗛挓鍐呭彧鑳藉彂涓€娆★紙Redis 闄愭祦锛岃秴闄?429锛?   - 6 浣嶉殢鏈洪獙璇佺爜锛孯edis 瀛?5 鍒嗛挓
   - 鐢?QQ SMTP 鍙戦€侊紝鏍囬銆屻€愰潚鍞€戞偍鐨勬敞鍐岄獙璇佺爜銆嶏紝鍙戜欢浜恒€岄潚鍞畼鏂瑰洟闃熴€?2. `POST /api/user/register` 鏃跺甫涓?`schoolEmail` 鍜?`emailCode`锛?   - 鏍￠獙閫氳繃鍚庣珛鍗冲垹闄?Redis 涓殑楠岃瘉鐮侊紙涓€娆℃€т娇鐢級
   - 閿欒鎴栬繃鏈熻繑鍥?400銆岄獙璇佺爜閿欒鎴栧凡杩囨湡銆?
## 鍚庣画浼樺寲璺嚎鍥撅紙鎸変紭鍏堢骇锛?
1. **瀹夊叏椋庢帶**锛欳ORS 鏀舵暃鍒版寮忓煙鍚嶏紱鎺ュ彛鎸夎鑹插仛鏂规硶绾ч壌鏉冿紙Spring Security + `@PreAuthorize`锛夛紱鐧诲綍/鍙戠爜鍔?IP 缁村害闄愭祦涓庡け璐ラ攣瀹氾紱鏁忔劅鎿嶄綔锛堝皝绂併€佸鏍革級鍐欏璁℃棩蹇椼€?2. **鎬ц兘涓庢灦鏋?*锛氬晢鍝佸垪琛ㄦ帴鍏?Elasticsearch 鎴?MySQL 鍏ㄦ枃绱㈠紩鏇夸唬 `LIKE %keyword%`锛涘浘鐗囪縼绉诲埌瀵硅薄瀛樺偍锛圤SS/MinIO锛? CDN锛涚儹鐐瑰垪琛ㄥ姞 Redis 缂撳瓨涓庣紦瀛樺嚮绌夸繚鎶わ紱璁㈠崟瓒呮椂娓呯悊鏀逛负寤惰繜闃熷垪锛堝 RocketMQ / Redis ZSet锛夈€?3. **浜ゆ槗涓庤祫閲戝畨鍏?*锛氱疆椤舵敹娆剧洰鍓嶆槸銆屾壂鐮?+ 浜哄伐瀹℃牳銆嶏紝涓婄嚎搴旀帴寰俊/鏀粯瀹濅紒涓氭敮浠樺苟鏍￠獙鍥炶皟绛惧悕锛涜鍗曞紩鍏ユ敮浠樻祦姘磋〃銆佸箓绛夐敭涓庡璐︿换鍔★紱骞冲彴鎶戒剑涓庣粨绠楄处鏈€?4. **鐢ㄦ埛浣撻獙**锛氬墠绔粠鍗曟枃浠舵媶鎴?Vite + Vue 宸ョ▼锛堢粍浠跺寲銆佽矾鐢便€佺姸鎬佺鐞嗭級锛涘晢鍝佽鎯呰ˉ SKU/澶氬浘杞挱锛涙秷鎭ā鍧楁帴瀹炴椂閫氱煡锛圵ebSocket锛夛紱楠ㄦ灦灞忎笌鍥剧墖鎳掑姞杞姐€?5. **杩愯惀涓庡晢涓氬彉鐜?*锛氬鍔犳洕鍏変綅/鎺ㄨ崘娴佺珵浠枫€佸崠瀹朵細鍛樺椁愩€佷氦鏄撴湇鍔¤垂锛涙暟鎹湅鏉匡紙DAU銆佹垚浜ら銆佺疆椤惰浆鍖栫巼锛夛紱浼樻儬鍒镐笌棣栧崟绔嬪噺銆?6. **鍙娴嬫€?*锛氭帴鍏?Prometheus + Grafana 鐩戞帶 QPS/寤惰繜/閿欒鐜囷紱缁撴瀯鍖栨棩蹇?+ TraceId锛涙參 SQL 鍛婅锛涘墠绔帴鍏?Sentry銆?7. **宸ョ▼璐ㄩ噺**锛氳ˉ JUnit + MockMvc 鎺ュ彛娴嬭瘯涓庡墠绔?E2E锛圥laywright锛夛紱CI 娴佹按绾匡紙鏋勫缓銆佹祴璇曘€侀暅鍍忔帹閫侊級锛涙暟鎹簱杩佺Щ宸ュ叿锛團lyway/Liquibase锛夋浛浠ｆ墜鍐?sql銆?8. **鍚堣涓庢不鐞?*锛氬疄鍚?瀛︾睄鏍￠獙銆佸唴瀹瑰鏍革紙鏁忔劅璇?+ 鍥剧墖閴撮粍锛夈€侀殣绉佹斂绛栦笌鏁版嵁鐣欏瓨绛栫暐銆佹湭鎴愬勾浜轰繚鎶ゃ€佹姇璇変妇鎶ヤ笌淇＄敤鎯╂垝瑙勫垯銆?
