/*??
 * COPYRIGHT (C) 2011 CHERIMOIA LLC. ALL RIGHTS RESERVED.
 *
 * THIS IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND/OR
 * MODIFY IT UNDER THE TERMS OF THE APACHE LICENSE, 
 * VERSION 2.0 (THE "LICENSE").
 *
 * THIS LIBRARY IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL,
 * BUT WITHOUT ANY WARRANTY; WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 *   
 * SEE THE LICENSE FOR THE SPECIFIC LANGUAGE GOVERNING PERMISSIONS 
 * AND LIMITATIONS UNDER THE LICENSE.
 *
 * You should have received a copy of the Apache License
 * along with this distribution; if not, you may obtain a copy of the 
 * License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 ??*/


package com.zotoh.cloudapi.aws;

import static com.zotoh.cloudapi.aws.AWSAPI.testForNotSubError;
import static com.zotoh.core.util.CoreUte.tstEStrArg;
import static com.zotoh.core.util.CoreUte.tstObjArg;
import static com.zotoh.core.util.LangUte.LT;
import static com.zotoh.core.util.LoggerFactory.getLogger;
import static com.zotoh.core.util.StrUte.isEmpty;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.DataFormat;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.platform.EndpointType;
import org.dasein.cloud.platform.PushNotificationSupport;
import org.dasein.cloud.platform.Subscription;
import org.dasein.cloud.platform.Topic;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.ListSubscriptionsRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsResult;
import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.zotoh.core.util.Logger;
import com.zotoh.core.util.Tuple;


/**
 * @author kenl
 *
 */
public class SNS implements PushNotificationSupport {

    private Logger ilog() {  return _log=getLogger(SNS.class);    }
    private transient Logger _log= ilog();
    public Logger tlog() {  return _log==null ? ilog() : _log;    }    
		
	private final AWSPlatformSvcs _svc;
	
	/**
	 * @param s
	 */
	protected SNS(AWSPlatformSvcs s) {
		tstObjArg("platform-service", s);
		_svc=s;
	}
	
	@Override
	public String confirmSubscription(String topic, String token, boolean authUnsubscribe)
					throws CloudException, InternalException {
		ConfirmSubscriptionResult res=_svc.getCloud().getSNS().confirmSubscription(
			new ConfirmSubscriptionRequest()
			.withAuthenticateOnUnsubscribe(authUnsubscribe?"true":"false")
			.withTopicArn(topic)
			.withToken(token)) ;
		return res==null ? null : res.getSubscriptionArn();
	}

	@Override
	public Topic createTopic(String topic) throws CloudException,
					InternalException {
	    tstEStrArg("topic-name", topic);
	    CreateTopicResult res=_svc.getCloud().getSNS().createTopic(
	            new CreateTopicRequest()
	            .withName(topic));
	    Topic t= null;
	    if (res != null) {
	        t= new Topic();
	        t.setActive(true);
	        t.setName(res.getTopicArn());
	        t.setProviderOwnerId(_svc.getCloud().getContext().getAccountNumber()) ;
	        t.setProviderRegionId(_svc.getCloud().getContext().getRegionId()) ;
	        t.setProviderTopicId(t.getName());
	    }
	    return t;
	}

	@Override
	public String getProviderTermForSubscription(Locale loc) {
		return "subscription";
	}

	@Override
	public String getProviderTermForTopic(Locale loc) {
		return "topic";
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
	    try {
	        _svc.getCloud().getSNS().listSubscriptions();
	        return true;
	    }
	    catch (AmazonServiceException e) {
	    	if (testForNotSubError(e, "SubscriptionCheckFailed" 
                    ,"AuthFailure" 
                    ,"SignatureDoesNotMatch" 
                    ,"InvalidClientTokenId", "OptInRequired"))  {
                return false;
            }
	    	else { throw new CloudException(e); }
	    }
	    catch (AmazonClientException e) {	
	        throw new InternalException(e);
	    }
	}

	@SuppressWarnings("unchecked")
    @Override
	public Collection<Subscription> listSubscriptions(String optionalTopicId)
					throws CloudException, InternalException {
	    List<com.amazonaws.services.sns.model.Subscription> subs;
	    List<Subscription> rc= LT();
	    Tuple t;
	    String token= null;
	    do {
	        if (isEmpty(optionalTopicId)) {
	            t=listSubs(token);
	        }
	        else {
	            t= listSubsWithTopic(optionalTopicId, token);
	        }
	        subs= (List<com.amazonaws.services.sns.model.Subscription>) t.get(0);
	        token=(String)t.get(1);
	        if (subs != null) for (int i=0; i < subs.size(); ++i) {
	            rc.add( toSub( subs.get(i)));	            
	        }
	    }
	    while (!isEmpty(token));
	    
		return rc;
	}
	
	/**/
    private Tuple listSubsWithTopic(String topic, String nextToken) {
        ListSubscriptionsByTopicRequest req= new ListSubscriptionsByTopicRequest().withTopicArn(topic);
        if (!isEmpty(nextToken)) { req.withNextToken(nextToken); }
        ListSubscriptionsByTopicResult res=_svc.getCloud().getSNS().listSubscriptionsByTopic(req);
        List<com.amazonaws.services.sns.model.Subscription> lst=null;
        String tkn=null;
        if (res != null) {
            lst=res.getSubscriptions();
            tkn=res.getNextToken();
        }
        return new Tuple(lst, tkn);
    }
    /**/
	private Tuple listSubs(String nextToken) {
	    ListSubscriptionsRequest req= new ListSubscriptionsRequest();
	    if (!isEmpty(nextToken)) { req.withNextToken(nextToken); }
        ListSubscriptionsResult res = _svc.getCloud().getSNS().listSubscriptions( req);
        List<com.amazonaws.services.sns.model.Subscription> lst=null;
        String tkn=null;
        if (res != null) {
            lst=res.getSubscriptions();
            tkn=res.getNextToken();
        }
        return new Tuple(lst, tkn);
	}

	@Override
	public Collection<Topic> listTopics() throws CloudException,
					InternalException {
	    List<Topic> rc= LT();
	    ListTopicsRequest req;
	    ListTopicsResult res;
	    String token=null;
	    List<com.amazonaws.services.sns.model.Topic> lst;
	    do {
	        req= new ListTopicsRequest();
	        if (!isEmpty(token)) { req.setNextToken(token); }
	        res= _svc.getCloud().getSNS().listTopics(req) ;
	        if (res != null) {
	            token= res.getNextToken();
	            lst= res.getTopics();
	        } else {
	            token=null;
	            lst=null;
	        }
	        if (lst != null) for (int i=0; i < lst.size(); ++i) {
	            rc.add( toTopic(lst.get(i)));
	        }
	    }
	    while (!isEmpty(token));
		return rc;
	}

	@Override
	public String publish(String topic, String subject, String message)
					throws CloudException, InternalException {
	    tstEStrArg("message", message);
        tstEStrArg("topic-name", topic);
        tstEStrArg("subject", subject);
        PublishResult res=_svc.getCloud().getSNS().publish(
	            new PublishRequest()
	            .withTopicArn(topic)
	            .withSubject(subject)
	            .withMessage(message));
        return res==null ? null : res.getMessageId() ;
	}

	@Override
	public void removeTopic(String topic) throws CloudException,
					InternalException {
	    tstEStrArg("topic-name", topic);
	    _svc.getCloud().getSNS().deleteTopic(
	            new DeleteTopicRequest().withTopicArn(topic));
	}

	@SuppressWarnings("unused")
	@Override
	public void subscribe(String topic, EndpointType type, DataFormat fmt,
					String endpt) throws CloudException, InternalException {
	    tstEStrArg("topic-name", topic);
        tstEStrArg("endpoint", endpt);
        SubscribeResult res=_svc.getCloud().getSNS().subscribe(
	            new SubscribeRequest()
	            .withProtocol(AWSAPI.toProtocol(fmt, type))
	            .withTopicArn(topic)
	            .withEndpoint(endpt));
        String subId= res==null ? null : res.getSubscriptionArn();
        //TODO strange, shouldn't it return subscription-id
	}

	@Override
	public void unsubscribe(String subscription) throws CloudException,
					InternalException {
	    tstEStrArg("subscription-name", subscription);
	    _svc.getCloud().getSNS().unsubscribe(
	            new UnsubscribeRequest().withSubscriptionArn(subscription));
	}

	/**/
	private Topic toTopic(com.amazonaws.services.sns.model.Topic t) {
	    Topic p= null;
	    if (t != null) {
	        p= new Topic();
            p.setName(t.getTopicArn());
	        p.setActive(true);
	        p.setDescription(p.getName());
	        p.setProviderOwnerId(_svc.getCloud().getContext().getAccountNumber());
	        p.setProviderTopicId(p.getName());
	        p.setProviderRegionId(_svc.getCloud().getContext().getRegionId());
	    }
	    return p;
	}
	
	/**/
	private Subscription toSub(com.amazonaws.services.sns.model.Subscription s) {
	    Subscription ss= null;
	    Tuple t;
	    if (s != null) {
	        ss= new Subscription();	        
	        ss.setName(s.getSubscriptionArn());
	        ss.setProviderOwnerId(s.getOwner());
	        ss.setProviderRegionId(_svc.getCloud().getContext().getRegionId());
	        ss.setProviderSubscriptionId(ss.getName());
	        ss.setProviderTopicId(s.getTopicArn());
            ss.setEndpoint(s.getEndpoint());
            t=AWSAPI.toDataFmt(s.getProtocol()) ;
            if (t != null) {
                ss.setEndpointType( (EndpointType) t.get(1));
                ss.setDataFormat((DataFormat) t.get(0));
            }
	    }
	    return ss;
	}
}
