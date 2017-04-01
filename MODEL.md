# Model


        CREATE 
          (`0` :User {username:'String',name:'String',email:'String',password:'String',hash:'String'}) ,
          (`1` :Post {status:'String',time:'Long'}) ,
          (`2` :Post ) ,
          (`3` :Post ) ,
          (`4` :User ) ,
          (`5` :User ) ,
          (`6` :Tag {name:'String'}) ,
          (`0`)-[:`POSTED_ON_2017_04_01`]->(`1`),
          (`0`)-[:`LIKES`]->(`2`),
          (`0`)-[:`REPOSTED_ON_2017_04_01`]->(`3`),
          (`4`)-[:`POSTED_ON_2017_03_31`]->(`3`),
          (`5`)-[:`POSTED_ON_2017_03_30`]->(`2`),
          (`0`)-[:`FOLLOWS`]->(`4`),
          (`0`)-[:`FOLLOWS`]->(`5`),
          (`2`)-[:`MENTIONED_ON_2017_03_30`]->(`0`),
          (`1`)-[:`REPLIED_TO`]->(`2`),
          (`1`)-[:`TAGGED_ON_2017_04_01`]->(`6`)
          
All relationships have a time:Long property.          