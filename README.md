# SpringBoot-AWS-SQS-Consumer-Producer-Code
This is Spring rest API for Post and Receive message from AWS Simple Queue Service (SQS)  


# Software Requirements 
  1. Java 8
  2. Maven 3.xx
  3. Any IDE (Eclipse or IntelliJ)
  4. Any RestClinet (PostMan)
  5. AWS Account
# Detail explanation & Youtube Video Link : https://youtu.be/s8iJI58-jNE
# Here I have used Spring Cloud AWS starter instead of AWS SDK
# Give a Star rating if this is useful

# use Application
1. Download the zip file and extract it
2. import as an existing maven project
3. build the project
4. create IAM user from AWS & provide Programmatic Access with FullSQSAdminAcccess
5. copy the Access ID & Secret Key and provide in APplication.properties file
6. Create a QUEUE from AWS SQS service (Select the region while creating the queue)
7. copy the queue name & Endpoint URL (provide in the Application.properties file and SQSLister)
