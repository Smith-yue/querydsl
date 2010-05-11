/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.query.jdoql.dml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.JoinType;
import com.mysema.query.QueryMetadata;
import com.mysema.query.dml.DeleteClause;
import com.mysema.query.jdoql.JDOQLSerializer;
import com.mysema.query.jdoql.JDOQLTemplates;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.path.PEntity;

/**
 * DeleteClause implementation for JDO
 * 
 * @author tiwe
 *
 */
public class JDOQLDeleteClause implements DeleteClause<JDOQLDeleteClause>{

    private final QueryMetadata metadata = new DefaultQueryMetadata();
    
    private final PersistenceManager persistenceManager;
    
    private final JDOQLTemplates templates;
    
    private final PEntity<?> entity;
    
    public JDOQLDeleteClause(PersistenceManager pm, PEntity<?> entity){
        this(pm, entity, JDOQLTemplates.DEFAULT);
    }
    
    public JDOQLDeleteClause(PersistenceManager persistenceManager, PEntity<?> entity, JDOQLTemplates templates){
        this.entity = entity;
        this.persistenceManager = persistenceManager;
        this.templates = templates;
        metadata.addJoin(JoinType.DEFAULT, entity);        
    }
    
    @Override
    public long execute() {
        Query query = persistenceManager.newQuery(entity.getType());
        if (metadata.getWhere() != null){            
            JDOQLSerializer serializer = new JDOQLSerializer(templates, entity);
            serializer.handle(metadata.getWhere());
            query.setFilter(serializer.toString());
            Map<Object,String> constToLabel = serializer.getConstantToLabel();    
            
            try{
                if (!constToLabel.isEmpty()) {
                    List<Object> constants = new ArrayList<Object>(constToLabel.size());
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<Object, String> entry : constToLabel.entrySet()){
                        if (builder.length() > 0){
                            builder.append(", ");
                        }
                        builder.append(entry.getKey().getClass().getName()).append(" ");
                        builder.append(entry.getValue());
                        constants.add(entry.getKey());
                    }                    
                    query.declareParameters(builder.toString());
                    return query.deletePersistentAll(constants.toArray());
                } else {
                    return query.deletePersistentAll();
                }    
            }finally{
                query.closeAll();    
            }            
        }else{
            try{
                return query.deletePersistentAll();
            }finally{
                query.closeAll();    
            }   
        }        
    }

    @Override
    public JDOQLDeleteClause where(EBoolean... o) {
        metadata.addWhere(o);
        return this;
    }

}
