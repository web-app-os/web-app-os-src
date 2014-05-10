package jp.co.headwaters.webappos.controller.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.headwaters.webappos.controller.ControllerConstants;
import jp.co.headwaters.webappos.controller.exception.NotFoundException;

/**
 * ページングユーティリティ
 */
public class PagingUtils {

	/** 全レコード件数 */
	public Integer recordCount;
	/** 画面表示レコード件数(limit) */
	public Integer perPage;
	/** 表示開始レコード番号(offset) */
	public Integer recordBeginNo;
	/** 表示終了レコード番号 */
	public Integer recordEndNo;
	/** ページング番号リスト */
	public ArrayList<String> pageNoList;
	/** 現在のページ番号 */
	public Integer pageNo;
	/** 前のページ番号 */
	public Integer prevPageNo;
	/** 次のページ番号 */
	public Integer nextPageNo;
	/** 表示開始ページング番号 */
	public Integer pagingBeginNo;
	/** 表示終了ページング番号 */
	public Integer pagingEndNo;
	/** 最終ページ番号 */
	public Integer maxPageNo;

	/**
	 * ページングに必要なカウント処理を取得
	 * @param resultMap 処理結果を保持するMap
	 * @param resultName result識別子
	 * @param recordCount 全レコード件数
	 * @param pageNo      遷移先のページ番号（1～
	 * @param limitPage   画面表示レコード件数
	 * @param pagingCount ページング表示件数
	 * @throws NotFoundException
	 */
	public PagingUtils(Map<String, Object> resultMap, String resultName,
			int recordCount, int pageNo, int limitPage, int pagingCount) throws NotFoundException {
		// 全レコード件数と画面表示レコード件数をフィールドに保持する
		this.recordCount = recordCount;
		this.perPage = (limitPage > 0) ? limitPage : 1;

		// ページング番号リストを生成する
		// 最大ページ番号を取得する
		this.maxPageNo = (int) Math.ceil(this.recordCount * 1.0d / this.perPage);

		if (this.maxPageNo == 0)
			this.maxPageNo = 1;

		// 現在のページ番号を取得する
		this.pageNo = (pageNo < 1) ? 1 : pageNo;
		if (this.pageNo > this.maxPageNo) {
			throw new NotFoundException();
		}
		// 前ページ番号を取得する
		this.prevPageNo = this.pageNo - 1;
		// 次ページ番号を取得する
		if (this.pageNo == this.maxPageNo) {
			//次のページが無い場合
			this.nextPageNo = 0;
		} else {
			//次のページがある場合
			this.nextPageNo = this.pageNo + 1;
		}

		// 表示開始ページング番号
		if (this.pageNo - pagingCount <= 0) {
			this.pagingBeginNo = 1;
		} else {
			this.pagingBeginNo = this.pageNo - pagingCount;
		}
		// 表示終了ページング番号
		this.pagingEndNo = this.pagingBeginNo + (pagingCount * 2) - 1;
		if (this.pagingEndNo > this.maxPageNo) {
			this.pagingEndNo = this.maxPageNo;
		}

		// ページング番号リストに表示開始ページング番号から表示終了ページング番号のInteger要素を追加する
		this.pageNoList = new ArrayList<String>();
		for (int i = this.pagingBeginNo; i <= this.pagingEndNo; i++) {
			this.pageNoList.add(String.valueOf(i));
		}

		// レコード番号を取得する
		Integer start;
		Integer end;
		// 表示開始レコード番号を取得する
		if (this.pageNo == 1) {
			start = 1;
		} else {
			start = ((this.pageNo - 1) * this.perPage) + 1;
		}
		// 表示終了レコード番号を取得する
		if (this.pageNo == this.maxPageNo) {
			end = this.recordCount.intValue();
		} else {
			end = this.pageNo * this.perPage;
		}
		// レコード件数が0件の場合
		if (this.recordCount == 0) {
			start = 0;
			end = 0;
			this.pageNoList = new ArrayList<String>();
			this.prevPageNo = 0;
			this.nextPageNo = 0;
		}

		// xx 件中 xx-xx 件を表示
		this.recordBeginNo = start;
		this.recordEndNo = end;

		resultMap.put(getPageInfoName(resultName), setPageInfo());
	}

	private static String getPageInfoName(String resultName){
		StringBuilder sb = new StringBuilder();
		sb.append(ControllerConstants.RESULT_MAP_KEY_PAGER);
		sb.append(ControllerConstants.REQUEST_PARAM_NAME_DELIMITER);
		sb.append(resultName.toUpperCase());
		return sb.toString();
	}

	private Map<String, Object> setPageInfo() {
		Map<String, Object> pageInfo = new HashMap<String, Object>();
		pageInfo.put(ControllerConstants.CRUD_PAGER_RECORD_COUNT, this.recordCount);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PER_PAGE, this.perPage);
		pageInfo.put(ControllerConstants.CRUD_PAGER_RECORD_BEGIN_NO, this.recordBeginNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_RECORD_END_NO, this.recordEndNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PAGE_NO, this.pageNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PREV_PAGE_NO, this.prevPageNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_NEXT_PAGE_NO, this.nextPageNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PAGING_BEGIN_NO, this.pagingBeginNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PAGING_END_NO, this.pagingEndNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_MAX_PAGE_NO, this.maxPageNo);
		pageInfo.put(ControllerConstants.CRUD_PAGER_PAGE_NO_LIST, this.pageNoList);
		return pageInfo;
	}
}
