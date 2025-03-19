package com.pancakeswap.nft.publish.controller;

import com.pancakeswap.nft.publish.exception.ListingException;
import com.pancakeswap.nft.publish.model.dto.collection.CollectionDataDto;
import com.pancakeswap.nft.publish.service.BunnyNFTService;
import com.pancakeswap.nft.publish.service.NFTService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.util.Optional;

import static com.pancakeswap.nft.publish.model.dto.response.CollectionListingFailedResponse.COLLECTION_LISTING_HAS_BEEN_INITIATED;
import static com.pancakeswap.nft.publish.model.dto.response.CollectionListingFailedResponse.FAILED_TO_LIST_COLLECTION;

/**
 * NFT收藏集控制器
 * 处理与NFT收藏集相关的请求，包括创建、添加和删除收藏集
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class CollectionController {

    private static final String SECURE_TOKEN = "x-secure-token";
    private static final Bandwidth LIMIT = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
    protected final Bucket bucket = Bucket.builder()
            .addLimit(LIMIT)
            .build();

    @Value(value = "${secure.token}")
    public String accessToken;

    private final NFTService nftService;
    private final BunnyNFTService bunnyNftService;

    public CollectionController(NFTService nftService, BunnyNFTService bunnyNftService) {
        this.nftService = nftService;
        this.bunnyNftService = bunnyNftService;
    }

    /**
     * 创建新的NFT收藏集
     * @param dataDto 收藏集创建请求参数，包含收藏集的基本信息
     * @param secureToken 安全令牌，用于验证请求权限
     * @return 如果创建成功返回成功消息，失败则返回错误信息
     */
    @PostMapping("/collections")
    public ResponseEntity<String> listCollection(@Valid @RequestBody CollectionDataDto dataDto,
                                                 @RequestHeader(value = SECURE_TOKEN) String secureToken) {
        Optional<ResponseEntity<String>> responseEntity = isRequestNotAllowed(secureToken);
        if (responseEntity.isPresent()) {
            return responseEntity.get();
        }
        try {
            nftService.isListingPossible(dataDto.getAddress()).thenRunAsync(() -> {
                try {
                    nftService.listNFTs(dataDto);
                } catch (Exception e) {
                    log.error(FAILED_TO_LIST_COLLECTION.getMessage(), e);
                }
            });
            return ResponseEntity.ok(COLLECTION_LISTING_HAS_BEEN_INITIATED.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 添加Bunny NFT到收藏集
     * @param address NFT合约地址
     * @param secureToken 安全令牌，用于验证请求权限
     * @return 如果添加成功返回成功消息，失败则返回错误信息
     */
    @PostMapping(path = "/bunny/collections/{address}")
    public ResponseEntity<String> addBunnyNFt(@PathVariable("address") String address,
                                              @RequestHeader(value = SECURE_TOKEN) String secureToken) {
        Optional<ResponseEntity<String>> responseEntity = isRequestNotAllowed(secureToken);
        if (responseEntity.isPresent()) {
            return responseEntity.get();
        }
        try {
            bunnyNftService.isListingPossible(address).thenRunAsync(() -> {
                try {
                    bunnyNftService.listNFTs(address);
                } catch (Exception e) {
                    log.error(FAILED_TO_LIST_COLLECTION.getMessage(), e);
                }
            });
            return ResponseEntity.ok(COLLECTION_LISTING_HAS_BEEN_INITIATED.getMessage());
        } catch (ListingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除指定的收藏集
     * @param collectionId 要删除的收藏集ID
     * @param secureToken 安全令牌，用于验证请求权限
     * @return 如果删除成功返回"Deleted"，失败则返回错误信息
     */
    @DeleteMapping(path = "/collections/{id}")
    public ResponseEntity<String> deleteCollection(@PathVariable("id") String collectionId,
                                                   @RequestHeader(value = SECURE_TOKEN) String secureToken) {
        Optional<ResponseEntity<String>> responseEntity = isRequestNotAllowed(secureToken);
        if (responseEntity.isPresent()) {
            return responseEntity.get();
        }
        if (nftService.deleteCollection(collectionId)) {
            return ResponseEntity.ok("Deleted");
        } else {
            return ResponseEntity.badRequest().body(FAILED_TO_LIST_COLLECTION.getMessage());
        }
    }

    /**
     * 验证请求是否允许执行
     * 检查安全令牌是否有效以及请求频率是否超限
     * @param secureToken 安全令牌
     * @return 如果请求不允许则返回相应的错误响应，允许则返回空
     */
    private Optional<ResponseEntity<String>> isRequestNotAllowed(String secureToken) {
        if (isValidToken(secureToken)) {
            return Optional.of(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!bucket.tryConsume(1)) {
            return Optional.of(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
        }
        return Optional.empty();
    }

    /**
     * 验证令牌是否有效
     * @param token 待验证的令牌
     * @return 如果令牌无效返回true，有效返回false
     */
    private boolean isValidToken(String token) {
        return !accessToken.equals(token);
    }
}
