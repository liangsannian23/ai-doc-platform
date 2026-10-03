package com.aidoc.aidocplatform.service.impl;

import com.aidoc.aidocplatform.config.RabbitMqConfig;
import com.aidoc.aidocplatform.dto.DocUpdateMessage;
import com.aidoc.aidocplatform.dto.DocumentQueryDTO;
import com.aidoc.aidocplatform.entity.Document;
import com.aidoc.aidocplatform.mapper.DocumentMapper;
import com.aidoc.aidocplatform.service.IDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document>
        implements IDocumentService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;   // 新增：发消息

    private static final long DOC_TTL = 60 * 60L; // 1小时

    /** 带缓存的文档详情：Cache Aside + 1小时TTL */
    @Override
    public Document getDocWithCache(Long docId) {
        String key = "doc:hot:" + docId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (Document) cached;
        }
        Document doc = this.getById(docId);
        if (doc != null) {
            redisTemplate.opsForValue().set(key, doc, DOC_TTL, TimeUnit.SECONDS);
        }
        return doc;
    }

    /** 新增文档：写库成功后发 CREATE 消息 */
    @Override
    @Transactional
    public boolean save(Document doc) {
        boolean ok = super.save(doc);
        if (ok) {
            sendDocMessage(doc.getId(), doc.getOwnerId(), "CREATE");
        }
        return ok;
    }

    @Override
    @Transactional
    public boolean updateById(Document doc) {
        boolean ok = super.updateById(doc);
        if (ok && doc.getId() != null) {
            // Write-Through：更新库后立即刷新缓存
            redisTemplate.opsForValue().set(
                    "doc:hot:" + doc.getId(),
                    this.getById(doc.getId()),
                    DOC_TTL, TimeUnit.SECONDS);
            // 发 UPDATE 消息
            sendDocMessage(doc.getId(), doc.getOwnerId(), "UPDATE");
        }
        return ok;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean ok = super.removeById(id); // MP 逻辑删除
        if (ok) {
            redisTemplate.delete("doc:hot:" + id);   // 清缓存
            sendDocMessage((Long) id, null, "DELETE"); // 发 DELETE 消息
        }
        return ok;
    }

    /** 统一封装：组装并发送文档变更消息 */
    private void sendDocMessage(Long docId, Long userId, String operation) {
        DocUpdateMessage msg = new DocUpdateMessage();
        msg.setDocId(docId);
        msg.setUserId(userId);
        msg.setOperation(operation);
        msg.setTimestamp(System.currentTimeMillis());
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, msg);
    }

    @Override
    public IPage<Document> queryMyDocuments(Long userId, DocumentQueryDTO queryDTO) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getOwnerId, userId);
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Document::getCategoryId, queryDTO.getCategoryId());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(Document::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Document::getContent, queryDTO.getKeyword()));
        }
        wrapper.orderByDesc(Document::getUpdateTime);
        Page<Document> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return this.page(page, wrapper);
    }
}