package com.example.global_meals_gradle.service;

import com.example.global_meals_gradle.dao.*;
import com.example.global_meals_gradle.entity.*;
import com.example.global_meals_gradle.req.*;
import com.example.global_meals_gradle.res.*;

import com.example.global_meals_gradle.constants.OperationType;
import com.example.global_meals_gradle.constants.ReplyMessage;
import com.example.global_meals_gradle.constants.TaxType;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

	@Autowired
	private OrderCartDao orderCartDao;

	@Autowired
	private OrderCartDetailsDao orderCartDetailsDao;

	@Autowired
	private ProductsDao productsDao;
	@Autowired
	private PromotionsGiftsDao promotionsGiftsDao;
//	@Autowired
//	private MembersDao membersDao;
	@Autowired
	private GlobalAreaDao globalAreaDao;
	@Autowired
	private RegionsDao regionsDao;

//	核心 API 1:同步購物車，包括刪除單品
//	 前端呼叫時機：使用者改了數量且「停手 1 秒後」（Debounce 防抖邏輯）
	@Transactional
	public CartViewRes syncItem(CartSyncReq req) {

		int currentCartId;

//		 步驟 1：判斷這是第一件商品（要建新車），還是後面加的商品（沿用舊車）
		if (req.getCartId() == null) {

//			 劇本分支 A：建新車
//        	A-1.準備一個裝新購物車的空殼子
			OrderCart newCart = new OrderCart();
//        	A-2:塞東西進去，讓newCart各個屬性完整
			newCart.setGlobalAreaId(req.getGlobalAreaId());
//			   字串轉列舉
			newCart.setOperationType(OperationType.valueOf(req.getOperationType()));

//			操作者：員工點餐存 staffId，顧客點餐存 memberId
			if ("STAFF".equals(req.getOperationType())) {
				newCart.setOperation(req.getStaffId()); // 員工點餐存 staffId
			} else {
				newCart.setOperation(req.getMemberId());
			}

//			 這裡必須用 Spring Data JPA 內建的 save(),「新建主表」時我們強烈建議用 save()！
//			 因為 save() 執行完，資料庫自動生成的 ID 會立刻被裝回 newCart.getId() 裡面。
//			 我們馬上就能拿到剛建好的「7號」，提供給後面的商品明細使用。原生 SQL 很難做到這點！
//            A-3:用DAO將塞滿req資料的OrderCart存到資料庫去
			orderCartDao.save(newCart);
//			 把剛建好的newCart的Id值獲取到，至此新購物車的id就已經知道了
			currentCartId = newCart.getId();

		} else {
//			 劇本分支 B：已經有車了,id沿用
			currentCartId = req.getCartId();
		}

//		 步驟 2：處理這筆商品 的增刪改
//		 這時候我們有了 currentCartId (購物車號碼)，以及 req 傳來的 productId 和 quantity

		if (req.getQuantity() == 0) {
//			 ==== 情境 2-1：刪除商品 ====
			orderCartDetailsDao.deleteByCartIdAndProductId(currentCartId, req.getProductId());

		} else {
//			 ==== 情境 2-2：數量大於 0，加入新商品 或 修改數量 ====
//			 先找找看，這台車裡面是不是已經有這個商品了
			OrderCartDetails existingDetail = orderCartDetailsDao.findByCartIdAndProductId(currentCartId,
					req.getProductId());

			if (existingDetail != null) {
//				 情況 A：購物車裡已經有了，單純把數量「覆蓋」成前端傳來的新數量
				existingDetail.setQuantity(req.getQuantity());

//				 再次呼叫 save()！在此它是 UPDATE 的意思 (因為 existingDetail 已經有流水號 id 了)
				orderCartDetailsDao.save(existingDetail);

			} else {
//				 情況 B：購物車裡還沒有，這是一筆全新的購物車明細，我們要建一筆新的 OrderCartDetails！
				OrderCartDetails newDetail = new OrderCartDetails();
				newDetail.setOrderCartId(currentCartId);
				newDetail.setProductId(req.getProductId());
//				 🚨 【大腦呼叫倉庫】去 Products 表查一下這個商品到底多少錢？
//				 思云有協助修改 -- 有問題問
				Products product = productsDao.findById(req.getProductId());
//				  商品不存在或已下架，拋例外讓前端知道
				if (product == null || !product.isActive()) {
					throw new RuntimeException("商品 ID " + req.getProductId() + " 不存在或已下架");
				}

				newDetail.setPrice(product.getBasePrice());
				newDetail.setQuantity(req.getQuantity());
				newDetail.setGift(false); // 客人選的，絕對不是贈品

//				 存進資料庫做 INSERT
				orderCartDetailsDao.save(newDetail);
			}
		}

//		 步驟 3：重新計算金額、滿額贈、包成 Res 回傳給畫面
		return getCartView(currentCartId, req.getMemberId());
	}

	/**
	 * 核心 API 2:刪除購物車內的特定商品
	 */
	@Transactional
	public CartViewRes removeItem(CartRemoveReq req) {
//		 1. 直接呼叫剛剛寫好的刪除 SQL
		orderCartDetailsDao.deleteByCartIdAndProductId(req.getCartId(), req.getProductId());

//		 2. 刪完之後，重新結算整台車的金額並回傳！ (預設沒有會員資訊就傳 null)
		return getCartView(req.getCartId(), req.getMemberId());
	}

//     API 3：使用者選擇贈品
//     selectedGiftProductId = null → 選「不要贈品」
//     selectedGiftProductId > 0    → 選了某個贈品

	@Transactional
	public CartViewRes selectGift(CartSelectGiftReq req) {
		/*
		 * 步驟 1：先把舊贈品全部清掉（避免一個人有兩份贈品），因為這個方法後面用的是： 客人選「不要」？ 👉 砍掉舊的（完成），結束。 客人選「換一個」？ 👉
		 * 砍掉舊的，建一個新的，結束。 OrderCartDetails giftDetail = new OrderCartDetails();
		 * orderCartDetailsDao.save(giftDetail); 資料庫不知道「客人現在是要把剛剛的牛肉湯 換成 大盤雞」，
		 * 資料庫只會覺得「老闆又叫我 加入 了一個大盤雞到購物車」，所以換一個贈品後是購物車列表顯示多個 贈品。這裡不用更換贈品而是直接刪除所有贈品的原因是：
		 * 如果前一個客人因為某種 Bug 一次有兩個贈品卡在裡面，你只更新一筆， Bug 還是會殘留。如果要選「不要贈品」，還要寫另一套邏輯去刪除。
		 */

		orderCartDetailsDao.deleteAllGiftsByCartId(req.getCartId());
//		步驟2-0：客人到底有沒有選：因為是Null的話就你不用在if下繼續了，直接return viewres
		Integer selectedId = req.getSelectedGiftProductId();
		if (selectedId != null && selectedId > 0) {
//	             使用者選了一個實際的贈品，進行一連串驗證
//	             2-1. 這一塊的作用是在檢查2裡面用的：重算小計，確認消費金額真的達標，所以要去撈資料庫購物車的所有商品+贈品明細算錢
			List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(req.getCartId());
//			一個 for 迴圈，跳過贈品（isGift()），把每筆的單價 × 數量加起來
			BigDecimal subtotal = BigDecimal.ZERO;
			for (OrderCartDetails d : allDetails) {
				if (!d.isGift()) {
					/*
					 * BigDecimal.valueOf(...)：這是 BigDecimal 類別提供的一個靜態工廠方法。 它的作用是將傳入的數值轉換成一個
					 * BigDecimal 物件，以便進行精確的數學計算。
					 */

					subtotal = subtotal.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
				}
			}
//	             2-2. 檢查1： promotions_gifts 表查一查資料庫裡有沒有選中的贈品對應的贈品規則
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftProductId(selectedId);
			if (giftRule == null) {
//	                 此贈品規則不存在或已下架，要回傳錯誤：new 一個 CartViewRes，setCode，setMessage，return 它
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}
//	             2-3. 檢查2：確認消費額有達到門檻
//			用檢查1裡面的重新計算出來的小計subtotal去和fullamount比較
			if (subtotal.compareTo(giftRule.getFullAmount()) < 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.NOT_REACH_FULLAMOUNT.getCode());
				err.setMessage(ReplyMessage.NOT_REACH_FULLAMOUNT.getMessage());
				return err;
			}
//	             2-4.檢查3： 確認贈品庫存（-1=無限量，0=已送完）
			if (giftRule.getQuantity() == 0) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_SEND_LIGHT.getCode());
				err.setMessage(ReplyMessage.GIFT_SEND_LIGHT.getMessage());
				return err;
			}
			/*
			 * 2-5. 檢查4：確認贈品作為商品本身上架並且存在在商品表中,因為贈品頁是商品，可能不選贈品，所以會是Null, 用內建的方法重寫了findById方法
			 * orElse(null)它的意思就是「如果盒子裡有商品就拿出來， 如果沒查到任何東西（或你傳了 Null 給它）就回傳 null
			 */

			Products giftProduct = productsDao.findById(selectedId).orElse(null);
			if (giftProduct == null || !giftProduct.isActive()) {
				CartViewRes err = new CartViewRes();
				err.setCode(ReplyMessage.GIFT_NOT_AVAILABLE.getCode());
				err.setMessage(ReplyMessage.GIFT_NOT_AVAILABLE.getMessage());
				return err;
			}
//	             2-6. 全部通過！把贈品寫進購物車明細
			OrderCartDetails giftDetail = new OrderCartDetails();
			giftDetail.setOrderCartId(req.getCartId()); // 關聯購物車
			giftDetail.setProductId(selectedId); // 贈品的商品 ID
			giftDetail.setPrice(BigDecimal.ZERO); // 贈品 0 元
			giftDetail.setQuantity(1); // 固定送 1 份
			giftDetail.setGift(true); // 標記為贈品
			giftDetail.setDiscountNote("滿額贈"); // 說明來源
			orderCartDetailsDao.save(giftDetail); // INSERT 進資料庫
		}
//顧客若是選擇不要贈品，先清空資料庫已選的贈品外後不會進入if，直接到這裡
		return getCartView(req.getCartId(), req.getMemberId());
	}

//	     API 4：清空購物車（一般商品 + 贈品全刪）

	@Transactional
	public CartViewRes clearCart(CartClearReq req) {
//	         刪除這台購物車的所有明細（一般商品 + 贈品全部清空）
		orderCartDetailsDao.deleteAllByCartId(req.getCartId());
//	         回傳空的購物車（items=空清單、subtotal=0、total=0）
		return getCartView(req.getCartId(), req.getMemberId());
	}

	/**
	 * api5：查看購物車內容 (例如前端一進到購物車頁面要拉取資料)
	 */
	public CartViewRes viewCart(int cartId, int memberId) {
//		 什麼都不用改，直接結算現有的東西！
		return getCartView(cartId, memberId);
	}

	/*
	 * 這個方法其實就類比結算機器，其他幾個api就是各自功能的服務生，最後一步都是到結算機器的這些功能 結算機器：檢查價格、重新驗證贈品、加稅金、打包成漂亮的
	 * CartViewRes）。 1.客人在首頁，點擊右上角的「🛒 購物車圖示」，滑出購物車側邊欄的那一瞬間。
	 * 2.或者客人點擊「前往結帳」，進入確認訂單頁面的那一瞬間。
	 * 做的6件事：1.先確認這個客人有沒有座位（購物車存在嗎）2.把桌上每道菜的現在價格都仔細核對一遍
	 * 3.看看他的免費贈品資格還有效嗎4.列出他還可以選哪些免費贈品5.加上這個地區的稅6.把完整的帳單列印出來給客人看
	 */
	@Transactional
	public CartViewRes getCartView(int cartId, int memberId) {
		CartViewRes res = new CartViewRes();
		res.setCartId(cartId);
		List<String> warningMessages = new ArrayList<>(); // 警告訊息（調價/下架）
		List<CartItemVO> voList = new ArrayList<>(); // 商品明細清單
		BigDecimal subtotal = BigDecimal.ZERO; // 稅前小計，從 0 開始

//	         ── 步驟 1：檢查1：確認傳進來的cartId對應的購物車存在嗎？──
		OrderCart cart = orderCartDao.findById(cartId);
		if (cart == null) {
			res.setCode(ReplyMessage.CART_NOT_FOUND.getCode());
			res.setMessage(ReplyMessage.CART_NOT_FOUND.getMessage());
			return res; // 直接返回，後面不用繼續
		}
//	         ── 步驟 2：檢查2：逐一驗算每筆「非贈品」的狀態與金額並組裝成商品VO、最後累加小計 ─
//		 ①：先撈出這台車的每一個購物車明細進行組裝成商品VO（購物計車明細是商品+贈品的）
//		這是最大的 for 迴圈，要做 3 件事：（A)檢查狀態（B）檢查金額：比對定價(C) 組裝 成商品VO、(D) 累加小計
		List<OrderCartDetails> allDetails = orderCartDetailsDao.findAllByCartId(cartId);
		for (OrderCartDetails detail : allDetails) {
			if (detail.isGift())
				continue; // 贈品留到步驟 3 處理
			/*
			 * ②先去查這個商品目前的狀態.分成-1.存在沒有下架（裡面又分成價格有變->需要更新orderCartDetails資料庫快照
			 * 、和價格和商品表的價格一樣）-2.下架或者不存在，為了 若是情況-2。商品VO倆面的
			 * setProductName會顯示XX已下架、setLineTotal會顯示0、商品單價就是加入購物車時的快照價、
			 * 然後加入警告warningMessages提示商品已下架或不存在
			 */
			Products product = productsDao.findById(detail.getProductId());
			CartItemVO vo = new CartItemVO();
//			（A）檢查狀態：
//			-2.如果商品已下架或不存在：
			if (product == null || !product.isActive()) {
//	           如果商品物件還在只是被下架了，就抓它的原名。
//             如果商品被刪除了，就抓 detail（訂單明細快照）裡的 productId 來當替代名稱。
				String name = (product != null) ? product.getName() : "商品 #" + detail.getProductId();
				vo.setProductName(name + "（已下架）");
				vo.setPrice(detail.getPrice()); // 顯示最後快照的價格
				vo.setLineTotal(BigDecimal.ZERO); // 不納入小計
				warningMessages.add("「" + name + "」已下架或不存在，請將其移除");
			} else {
//				-1：
				vo.setProductName(product.getName());
//	              (B)檢查金額： 比對定價： 偵測到調價：警告 + 更新orderCartDetails資料庫快照
				if (detail.getPrice().compareTo(product.getBasePrice()) != 0) {
					warningMessages.add("「" + product.getName() + "」的價格已從 $" + detail.getPrice() + " 調整為 $"
							+ product.getBasePrice());
					detail.setPrice(product.getBasePrice()); // 更新快照
					orderCartDetailsDao.save(detail); // 更新orderCartDetails資料庫快照
				}
//				 ③(C) ：組裝一個 商品VO（給前端看的商品卡片）
				vo.setDetailId(detail.getId());
				vo.setProductId(detail.getProductId());
				vo.setQuantity(detail.getQuantity());
				vo.setGift(false);
				vo.setDiscountNote(detail.getDiscountNote());
				BigDecimal lineTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())); // 單價 × 數量
				vo.setPrice(detail.getPrice());
				vo.setLineTotal(lineTotal);
//				④（D）累加小計
				subtotal = subtotal.add(lineTotal);
			}
			voList.add(vo);
		}

		/*
		 * 步驟 3：重新驗證使用者“已選的贈品”是否仍然有效 :4 個條件全部滿足，贈品才有效： ① 贈品規則存在、② 贈品商品上架、③ 行銷庫存充足、④
		 * 金額仍然達標
		 */
//	      先抓出這台購物車裡是否已經有已經選好的贈品了：如果有選好的看是否還有效，還沒有選好贈品的情況就創建贈品列表
		List<OrderCartDetails> existingGifts = orderCartDetailsDao.findSelectGiftsByCartId(cartId);
		if (!existingGifts.isEmpty()) {
			OrderCartDetails existingGift = existingGifts.get(0); // 購物車只會有一個贈品
			Products giftProduct = productsDao.findById(existingGift.getProductId());
//			根據購物車詳情裡的選中的贈品gift，獲取gift的ProductId，通過ProductId尋找商品表的這個贈品的狀態還有贈品表裡這個贈品對應的上架的活動
			PromotionsGifts giftRule = promotionsGiftsDao.findActiveRuleByGiftProductId(existingGift.getProductId());
//	             判斷贈品是否「依然有效」（4 個條件全部滿足）：
			boolean giftStillValid = giftRule != null // 規則存在（上架上面findActiveRuleByGiftProductId已經判斷）
					&& giftProduct != null && giftProduct.isActive() // 贈品作為商品狀態：存在並上架才醒
					&& (giftRule.getQuantity() == -1 || giftRule.getQuantity() > 0) // 還有庫存
					&& subtotal.compareTo(giftRule.getFullAmount()) >= 0; // 金額達標
			if (giftStillValid) {
//	                 有效：把贈品 VO 加入CartItemVO的清單voList中顯示在購物車界面
				CartItemVO giftVO = new CartItemVO();
				giftVO.setDetailId(existingGift.getId());
				giftVO.setProductId(existingGift.getProductId());
				giftVO.setProductName(giftProduct.getName()); // 展示贈品名稱（如「大盤雞」）
				giftVO.setQuantity(1);
				giftVO.setPrice(BigDecimal.ZERO);
				giftVO.setLineTotal(BigDecimal.ZERO);
				giftVO.setGift(true);
				giftVO.setDiscountNote("滿額贈");
				voList.add(giftVO);
			} else {
//	                 失效：自動刪除全部贈品，並告知前端
				orderCartDetailsDao.deleteAllGiftsByCartId(cartId);
				warningMessages.add("贈品資格已失效（消費金額不足或贈品已下架），請重新選擇");
			}
		}
//	         ── 步驟 4：如果不進入上面的if，就會到這裡也就是還沒有選贈品的時候：在步驟2裡我計算出了正確的小計，現在建立「可選贈品的清單」 ──
		List<AvailableGiftVO> availableGifts = new ArrayList<>();
//		把所有上架的贈品活動撈出來
		List<PromotionsGifts> activeGiftRules = promotionsGiftsDao.findAllActiveGifts();
//		逐一審查每條規則：-1.看消費達到了哪些規則門檻-2.遍歷每條達到門檻的規則的情況下，裡面的贈品是否在商品表存在並且還是上架
		for (PromotionsGifts rule : activeGiftRules) {
			if (subtotal.compareTo(rule.getFullAmount()) >= 0) {
	    /*消費金額達到此規則的門檻，就將AvailableGiftVO加入可選清單，只不過如果規則對應的贈品1.如過在商品表裡是
	     * 下架或者Null,2.贈品名額沒了3.贈品名額有，商品表裡的贈品存在並上架，這三種情況的
	     * AvailableGiftVO的GiftProductName、Available、UnavailableReason設置不同
	     * 最後就是依次判斷後加入贈品列表裡
	     */
				AvailableGiftVO option = new AvailableGiftVO();
				option.setGiftRuleId(rule.getId());
				option.setGiftProductId(rule.getGiftProductId());
				option.setFullAmount(rule.getFullAmount());

//				根據贈品表的gift_product_id去商品表查詢贈品在商品表的資訊
				Products giftAsProd = productsDao.findById(rule.getGiftProductId());
				if (giftAsProd == null || !giftAsProd.isActive()) {
//	                     贈品商品已下架，前端顯示為灰色不可點
					String gName = (giftAsProd != null) ? giftAsProd.getName() : "贈品 #" + rule.getGiftProductId();
					option.setGiftProductName(gName);
					option.setAvailable(false);
					option.setUnavailableReason("「" + gName + "」已下架");
				}
				else if (rule.getQuantity() == 0) {
//	                     贈品已送完，前端顯示為灰色不可點
					option.setGiftProductName(giftAsProd.getName());
					option.setAvailable(false);
					option.setUnavailableReason("「" + giftAsProd.getName() + "」贈品已送完");
				} 
				else {
//	                     可以選
					option.setGiftProductName(giftAsProd.getName());
					option.setAvailable(true);
					option.setUnavailableReason(null);
				}
				availableGifts.add(option);
			}
		}
//	         ── 步驟 5：查稅務設定並計算稅額 ──
		TaxInfoVO taxInfo = new TaxInfoVO();
		BigDecimal totalAmount = subtotal; // 預設（無稅設定時/內含稅）：總計 = 小計
		GlobalArea area = globalAreaDao.findById(cart.getGlobalAreaId());
		if (area != null) {
			Regions region = regionsDao.findByCountry(area.getCountry());
			if (region != null) {
				taxInfo.setTaxRate(region.getTaxRate()); 
//				如果它原本是 TaxType.EXCLUSIVE 的enum物件。加上 .name() 後，它身上的一層皮就被剝下來，變成純粹的英文字串 "EXCLUSIVE"。
				taxInfo.setTaxType(region.getTaxType().name()); 
				BigDecimal taxAmount;
				if (region.getTaxType() == TaxType.EXCLUSIVE) {
//	                     外加稅：稅額 = 小計 × 稅率，總計 = 小計 + 稅額
					taxAmount = subtotal.multiply(region.getTaxRate()).setScale(2, RoundingMode.HALF_UP);
					totalAmount = subtotal.add(taxAmount);
				} else {
//	                     內含稅：“稅額”=從含稅金額反推 = 小計 × 稅率 ÷ (1 + 稅率)
					taxAmount = subtotal.multiply(region.getTaxRate()).divide(BigDecimal.ONE.add(region.getTaxRate()),
							2, RoundingMode.HALF_UP);
					totalAmount = subtotal; // 內含稅：總計就是小計，不另加稅
				}
				taxInfo.setTaxAmount(taxAmount);
			}
		}
//	         ── 步驟 6：打包所有結果回傳 ──
		/*voList就是cartItem（包括贈品）的list
		 * availableGifts是 可選贈品下拉清單
		 * subtotal是小計，taxInfo是稅務資訊，totalAmount是最終總計，
		 * warningMessages警告訊息（空清單代表一切正常）
		 */
		res.setItems(voList);
		res.setSubtotal(subtotal);
		res.setAvailableGifts(availableGifts); 
		res.setTaxInfo(taxInfo); 
		res.setTotalAmount(totalAmount); 
		res.setWarningMessages(warningMessages); 
		res.setCode(ReplyMessage.SUCCESS.getCode());
		res.setMessage(ReplyMessage.SUCCESS.getMessage());
		return res;
	}
}
