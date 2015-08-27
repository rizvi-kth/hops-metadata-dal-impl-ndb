/*
 * Hops Database abstraction layer for storing the hops metadata in MySQL Cluster
 * Copyright (C) 2015  hops.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.hops.metadata.ndb.dalimpl.yarn;

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
import io.hops.metadata.yarn.dal.NextHeartbeatDataAccess;
import io.hops.metadata.yarn.entity.NextHeartbeat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NextHeartbeatClusterJ
    implements TablesDef.NextHeartbeatTableDef, NextHeartbeatDataAccess<NextHeartbeat> {

    private static final Log LOG = LogFactory.getLog(NextHeartbeatClusterJ.class);
  @PersistenceCapable(table = TABLE_NAME)
  public interface NextHeartbeatDTO extends RMNodeComponentDTO {

    @PrimaryKey
    @Column(name = RMNODEID)
    String getrmnodeid();

    void setrmnodeid(String rmnodeid);

    @Column(name = NEXTHEARTBEAT)
    int getNextheartbeat();

    void setNextheartbeat(int Nextheartbeat);
    
    @Column(name = PENDING_EVENT_ID)
    int getpendingeventid();

    void setpendingeventid(int pendingeventid);
    
  }

  private final ClusterjConnector connector = ClusterjConnector.getInstance();

  @Override
  public Map<String, Boolean> getAll() throws StorageException {

    HopsSession session = connector.obtainSession();
    HopsQueryBuilder qb = session.getQueryBuilder();
    HopsQueryDomainType<NextHeartbeatDTO> dobj =
        qb.createQueryDefinition(NextHeartbeatDTO.class);
    HopsQuery<NextHeartbeatDTO> query = session.createQuery(dobj);
    List<NextHeartbeatDTO> results = query.getResultList();

    return createMap(results);
  }

  @Override
  public boolean findEntry(String rmnodeId) throws StorageException {
    HopsSession session = connector.obtainSession();
    NextHeartbeatDTO nextHBDTO = session.find(NextHeartbeatDTO.class, rmnodeId);
    if (nextHBDTO != null) {
      return createHopNextHeartbeat(nextHBDTO).isNextheartbeat();
    }
    return false;
  }

  @Override
  public void updateNextHeartbeat(String rmnodeid, boolean nextHeartbeat,int pendingId)
      throws StorageException {
    HopsSession session = connector.obtainSession();
    if(nextHeartbeat){
      LOG.info("updating the database as true");
      session.savePersistent(createPersistable(new NextHeartbeat(rmnodeid,
            nextHeartbeat,pendingId), session));
    } else {
      session.remove(createPersistable(new NextHeartbeat(rmnodeid,
              true,pendingId), session));
    }
    //session.flush();
  }

  private NextHeartbeatDTO createPersistable(NextHeartbeat hopNextHeartbeat,
      HopsSession session) throws StorageException {
    NextHeartbeatDTO DTO = session.newInstance(NextHeartbeatDTO.class);
    //Set values to persist new persistedEvent
    DTO.setrmnodeid(hopNextHeartbeat.getRmnodeid());
    DTO.setNextheartbeat(booleanToInt(hopNextHeartbeat.isNextheartbeat()));
    DTO.setpendingeventid(hopNextHeartbeat.getPendingEventId());
    return DTO;
  }

  public static NextHeartbeat createHopNextHeartbeat(
      NextHeartbeatDTO nextHBDTO) {
    return new NextHeartbeat(nextHBDTO.getrmnodeid(), intToBoolean(nextHBDTO.
        getNextheartbeat()),nextHBDTO.getpendingeventid());
  }

  private Map<String, Boolean> createMap(List<NextHeartbeatDTO> results) {
    Map<String, Boolean> map = new HashMap<String, Boolean>();
    for (NextHeartbeatDTO persistable : results) {
      map.put(persistable.getrmnodeid(), intToBoolean(persistable.
          getNextheartbeat()));
    }
    return map;
  }

  /**
   * As ClusterJ boolean is buggy, we use Int to store the boolean field to NDB
   * and we convert it here to integer.
   * <p/>
   *
   * @return
   */
  private static boolean intToBoolean(int a) {
    return a == NEXTHEARTBEAT_TRUE;
  }

  private int booleanToInt(boolean a) {
    return a ? NEXTHEARTBEAT_TRUE : NEXTHEARTBEAT_FALSE;
  }

}
