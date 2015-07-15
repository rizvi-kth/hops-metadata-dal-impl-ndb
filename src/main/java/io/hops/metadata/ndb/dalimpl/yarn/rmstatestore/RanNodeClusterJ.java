/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.hops.metadata.ndb.dalimpl.yarn.rmstatestore;

import com.mysql.clusterj.annotation.Column;
import com.mysql.clusterj.annotation.PersistenceCapable;
import com.mysql.clusterj.annotation.PrimaryKey;
import io.hops.exception.StorageException;
import io.hops.metadata.ndb.ClusterjConnector;
import io.hops.metadata.ndb.wrapper.HopsQuery;
import io.hops.metadata.ndb.wrapper.HopsQueryBuilder;
import io.hops.metadata.ndb.wrapper.HopsQueryDomainType;
import io.hops.metadata.ndb.wrapper.HopsSession;
import io.hops.metadata.yarn.TablesDef;
import io.hops.metadata.yarn.dal.rmstatestore.RanNodeDataAccess;
import io.hops.metadata.yarn.entity.rmstatestore.RanNode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanNodeClusterJ implements
        TablesDef.RanNodeTableDef,
        RanNodeDataAccess<RanNode> {

  @PersistenceCapable(table = TABLE_NAME)
  public interface RanNodeDTO {

    @PrimaryKey
    @Column(name = APPLICATIONATTEMPTID)
    String getapplicationattemptid();

    void setapplicationattemptid(String applicationid);

    @PrimaryKey
    @Column(name = NODEID)
    String getnodeid();

    void setnodeid(String appstate);

  }

  private final ClusterjConnector connector = ClusterjConnector.getInstance();

  @Override
  public void addAll(Collection<List<RanNode>> toAdd)
          throws StorageException {
    HopsSession session = connector.obtainSession();
    List<RanNodeDTO> toPersist = new ArrayList<RanNodeDTO>();
    for (List<RanNode> l : toAdd) {
      for (RanNode n : l) {
        toPersist.add(createPersistable(n, session));
      }
    }
    session.savePersistentAll(toPersist);
  }

  @Override
  public Map<String,List<RanNode>> getAll() throws StorageException{
    HopsSession session = connector.obtainSession();
    HopsQueryBuilder qb = session.getQueryBuilder();
    HopsQueryDomainType<RanNodeDTO> dobj = qb.
        createQueryDefinition(RanNodeDTO.class);
    HopsQuery<RanNodeDTO> query = session.createQuery(dobj);
    List<RanNodeDTO> results = query.getResultList();

    return createMap(results);
  }
  
  
  private RanNodeDTO createPersistable(RanNode hop,
          HopsSession session) throws StorageException {
    RanNodeDTO updatedNodeDTO = session.newInstance(RanNodeDTO.class);
    updatedNodeDTO.setapplicationattemptid(hop.getApplicationAttemptId());
    updatedNodeDTO.setnodeid(hop.getNodeId());

    return updatedNodeDTO;
  }
  
   private Map<String, List<RanNode>> createMap(
      List<RanNodeDTO> results) throws StorageException {
    Map<String, List<RanNode>> map =
        new HashMap<String, List<RanNode>>();
    for (RanNodeDTO persistable : results) {
      RanNode hop =
          createHopRanNode(persistable);
      if (map.get(hop.getApplicationAttemptId()) == null) {
        map.put(hop.getApplicationAttemptId(),
            new ArrayList<RanNode>());
      }
      map.get(hop.getApplicationAttemptId()).add(hop);
    }
    return map;
  }
   
     private RanNode createHopRanNode(
      RanNodeDTO entry) throws StorageException {
      return new RanNode(entry.getapplicationattemptid(),entry.getnodeid());
  }
}
