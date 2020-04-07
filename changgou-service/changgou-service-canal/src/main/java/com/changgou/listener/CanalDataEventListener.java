package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.changou.content.feign.ContentFeign;
import com.changou.content.pojo.Content;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.ListenPoint;
import com.xpand.starter.canal.annotation.UpdateListenPoint;
import entity.Result;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Description 实现对表DML(增 、 删 、 改)操作的监听
 * @Author tangKai
 * @Date 11:46 2020/1/4
 **/
@CanalEventListener
@AllArgsConstructor
public class CanalDataEventListener {

    ContentFeign contentFeign;

    StringRedisTemplate stringRedisTemplate;

    /**
     * @Description 自定义监听器
     * @Author tangKai
     * @Date 17:17 2020/1/4
     * @Return void
     **/
    @ListenPoint(destination = "example", schema = "changgou_content", table = {"tb_content"}, eventType = {
        EventType.INSERT, EventType.UPDATE})
    public void onEventContent(EntryType entryType, RowData rowData) {
        // 获取分类id
        String categoryId = getColumnValue(rowData, "category_id");
        // 通过分类id查询广告列表
        Result<List<Content>> result = contentFeign.findByCategory(Long.parseLong(categoryId));
        // 存入redis
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(result.getData()));
    }


    /**
     * @Description 获取对应列的值
     * @Author tangKai
     * @Date 11:58 2020/1/4
     * @Param [rowData, columnName]
     * @Return java.lang.String
     **/
    private String getColumnValue(RowData rowData, String columnName) {
        List<Column> list = rowData.getAfterColumnsList();
        for (Column column : list) {
            if (columnName.equals(column.getName())) {
                return column.getValue();
            }
        }
        return null;
    }


    /**
     * @Description 监听新增的数据
     * @Author tangKai
     * @Date 11:59 2020/1/4
     * @Param [entryType, rowData]
     * @Return void
     **/
    @UpdateListenPoint
    public void onEventUpdate(EntryType entryType, RowData rowData) {
        List<Column> beforeList = rowData.getBeforeColumnsList();
        for (Column column : beforeList) {
            System.out.println();
            System.out.println();
            System.out
                .println("***********新增前***********" + "列名：" + column.getName() + "<---->列值：" + column.getValue());
        }
        System.out.println("--------------------------------更新前后--------------------------------");

        List<Column> afterList = rowData.getAfterColumnsList();
        for (Column column : afterList) {
            System.out
                .println("***********新增后***********" + "列名：" + column.getName() + "<---->列值：" + column.getValue());
            System.out.println();
            System.out.println();
        }
    }

    /**
     * @Description 监听删除的数据
     * @Author tangKai
     * @Date 14:13 2020/1/4
     * @Param [entryType, rowData]
     * @Return void
     **/
    @DeleteListenPoint
    public void onEventDelete(EntryType entryType, RowData rowData) {
        List<Column> beforeList = rowData.getBeforeColumnsList();
        for (Column column : beforeList) {
            System.out
                .println("***********删除数据***********" + "列名：" + column.getName() + "<---->列值：" + column.getValue());
        }
    }


}
