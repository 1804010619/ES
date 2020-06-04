package com.wnag.esapi;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 高级客户端API测试
 */
@SpringBootTest
class EsApiApplicationTests {
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    /**
     * 测试索引的创建  Request
     */
    @Test
    public void testCreateIndex() throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("wang_index");
        //执行请求 IndicesClient,请求后获得响应
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println(response);
    }

    /**
     * 测试获取索引
     */
    @Test
    public void testExistIndex() throws Exception {
        GetIndexRequest request = new GetIndexRequest("wang_index");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 测试删除索引
     */
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("wang_index");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 测试添加文档
     */
    @Test
    public void testAddDocument() throws IOException {
        //创建对象
        User user = new User("王利民", 21);
        //创建请求
        IndexRequest request = new IndexRequest("wang_index");
        //规则 put/index/_doc
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));
        //将数据放入请求
        request.source(JSON.toJSONString(user), XContentType.JSON);

        //客户端发送请求,获取响应结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        System.out.println(response.toString());
        System.out.println(response.status());
    }

    /**
     * 获取文档，判断是否存在
     * get /index/_doc/1
     */
    @Test
    public void testIsExists() throws IOException {
        GetRequest request = new GetRequest("wang_index", "1");

        //不获取返回的_source的上下文
        request.fetchSourceContext(new FetchSourceContext(false));

        request.storedFields("_none_");

        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    /**
     * 获取文档信息
     */
    @Test
    public void testGetDocument() throws Exception{
        GetRequest request = new GetRequest("wang_index", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String source = response.getSourceAsString();
        System.out.println(source); //打印文档内容
        System.out.println(response);   //返回的全部内容和命令是一样的
    }

    /**
     * 更新文档信息
     */
    @Test
    public void testUpdateDocument() throws Exception{
        UpdateRequest request = new UpdateRequest("wang_index", "1");
        request.timeout("1s");
        User user = new User("狂神说Java",23);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    /**
     * 删除文档信息
     */
    @Test
    public void testDeleteDocument() throws Exception{
        DeleteRequest request = new DeleteRequest("wang_index","1");
        request.timeout("1s");
        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    /**
     * 特殊的，真的项目一般都会批量插入数据
     */
    @Test
    public void testBulkkRequest() throws Exception{
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        ArrayList<User> list = new ArrayList<>();
        list.add(new User("wang_1",1));
        list.add(new User("wang_2",2));
        list.add(new User("wang_3",3));
        list.add(new User("wang_4",4));
        list.add(new User("wang_5",5));
        list.add(new User("wang_6",6));
        list.add(new User("wang_7",7));
        list.add(new User("wang_8",8));
        list.add(new User("wang_9",9));
        list.add(new User("wang_10",10));

        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(
                    new IndexRequest("wang_index")
                            .id(""+(i+1)).source(JSON.toJSONString(list.get(i)),
                            XContentType.JSON));
        }
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        System.out.println(response.hasFailures());
    }

    /**
     * 查询
     */
    @Test
    public void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("wang_index");
        //构建搜索的条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //查询条件，可以使用QueryBuilders工具类来实现
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "wang_2");
        builder.query(termQueryBuilder);
        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(builder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        SearchHit[] hits = searchResponse.getHits().getHits();
        Map<String, Object> map = hits[0].getSourceAsMap();
        System.out.println(map);
    }

}
