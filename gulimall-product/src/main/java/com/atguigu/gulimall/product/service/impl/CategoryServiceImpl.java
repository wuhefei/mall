package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog3Vo;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.mysql.cj.util.TimeUtil;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前的菜单是否被别的地方所引用
        //categoryDao.deleteBatchIds(asList); 这句和下一句是一样的哦！
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);


        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * 写模式下的清楚缓存：CacheEvict
     * 修改商品分类之后，value = {"category"},key = "#root.method.name"这个就会被删除掉。
     * @param category
     *
     * 1. 同时进行多个缓存操作可以使用@Caching进行组合。  @Caching(evict = {
     *             @CacheEvict(value = {"category"},key = "'getLeve1Categorys'"),
     *             @CacheEvict(value = {"category"},key = "'getCatelogJson'")
     *     })
     * 2.对分区进行统一删除：@CacheEvict(value = {"category"},allEntries = true)
     *
     */
    //@CacheEvict(value = {"category"},key = "'getLeve1Categorys'")
    /*@Caching(evict = {
            @CacheEvict(value = {"category"},key = "'getLeve1Categorys'"),
            @CacheEvict(value = {"category"},key = "'getCatelogJson'")
    })*/
    @CacheEvict(value = {"category"},allEntries = true)
    //@CachePut//双写模式，把更新后的结果重新放入redis中，前提：需要把修改之后的结果返回才行，此方法需要有返回值，暂返回void，故不支持
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    /**
     * 获取一级分类的列表
     * @return
     * 每一个需要缓存的数据我们都 可以来指定要放到那个名字的缓存【缓存的分区（按业务类型分）】
     * 指定缓存的key
     * 指定缓存存活时间
     * 将记录保存为json
     *  key 值设置：spel :https://docs.spring.io/spring-framework/docs/5.1.17.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *  写模式下的缓存@Cacheable
     */
    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLeve1Categorys() {
        System.out.println("getLeve1Categorys----------------->");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntityList;
    }
    /**
     * 第一次查询的所有 CategoryEntity 然后根据 parent_cid去这里找
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parent_cid) {

        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

    /**
     * 大道至简，直接使用注解解决一切问题。
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("----------------》查询数据库");
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        /**
         * 1.将数据库的多次查询变为1次。提高效率。
         */
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        /**
         * 1空结果缓存：解决缓存穿透问题
         * 2设置过期时间（加随机值）：解决缓存雪崩问题
         * 3加锁：解决缓存击穿问题
         */

        //加入缓存逻辑，缓存中的数据是json字符串
        //json跨语言，跨平台兼容
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中....查询数据库...");
            //2.缓存中没有，取数据库中查找 并把结果存入redis,然后返回结果
            Map<String, List<Catelog2Vo>> catelogJsonFromBd = getCatelogJsonFromBdWithRedissonLock();//从redis中查询了

        }
        System.out.println("缓存命中....直接返回...");
        //如果缓存中有则直接返回
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    //使用Redisson lock 这个代码就简单多了
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromBdWithRedissonLock() {
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    //redis的分布式锁set locak haha NX 占坑原理，原子性的仅有一个能占坑成功
    //@Override
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromBdWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        //加锁并设置过期时间，避免中途出现问题没有来得及删除，而别的人占不到坑的情况。
        //1.占分布式锁，去redis占坑
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "111");
        if (lock) {
            System.out.println("获取分布式成功...");
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            //加锁成功，执行业务
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //删除锁，为了实现删除的原子性需要使用luo脚本去执行，查询到有就删除。
                // 获取对比值和对比成功删除锁也是要同步的、原子的执行  参照官方使用lua脚本解锁
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }
            //业务完成后释放锁
            redisTemplate.delete("lock");
            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败.... 等待重试...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //加锁失败，自旋，充实            //可以休眠100ms
            return getCatelogJsonFromBdWithRedisLock();//自旋
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            //如果缓存中有则直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("----------------》查询数据库");
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        /**
         * 1.将数据库的多次查询变为1次。提高效率。
         */
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        //3.数据库中查到后，重新存入radis
        /**
         * 1空结果缓存：解决缓存穿透问题
         * 2设置过期时间（加随机值）：解决缓存雪崩问题
         * 3加锁：解决缓存击穿问题
         */
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return parent_cid;
    }

    //本地锁this
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromBdWithLocalLock() {

        //并发的情况，只有一个拿到多对象先去插数据库，查完之后就会忘reids中放入数据，
        //后面的拿到锁之后先去缓存中看下是否有数据，有的话直接返回不再插数据库
        //bean都是单利的，所以this是同一个，但是集群情况下this为一个容器一个。
        synchronized (this){
            return getDataFromDb();
        }

    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;

    }
    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}