<?php
error_reporting(0);
date_default_timezone_set('America/Los_Angeles');


new RealEstate();

class RealEstate{

    private $input_data = array();

    function __construct(){
        $xml = '';
        if($this->inputCollect()){
            $xml = $this->retrieveXML();
        }
        $this->view($xml);
    }
    
    function inputCollect(){

        if(!empty($_GET)){
            foreach($_GET as $id => $v){
                $this->input_data[$id] = $v;
            }
        }

        if(!isset($this->input_data['street']) || 
            !isset($this->input_data['city']) ||
            !isset($this->input_data['state']) 
        ){
            $this->input_data['street'] = $this->input_data['city'] =  $this->input_data['state'] ='';
            return false;
        }
        return true;
    }

    function retrieveXML(){
        $query = array(
            'zws-id' => 'X1-ZWz1dxqn5v8yrv_1y8f9', 
            'address' => $this->input_data['street'],
            'citystatezip' => $this->input_data['city'].', '.$this->input_data['state'],
            'rentzestimate' => true
        );
        

        $url = "http://www.zillow.com/webservice/GetDeepSearchResults.htm?".http_build_query($query);
        echo $url;
        $xml = simplexml_load_file($url);

        return $xml;
    }

    function resultHTML($xml){
        if(!isset($xml) || empty($xml) ){
            return '';
        }
        if($xml->message->code != 0){
            return '<h4>No exact match found--Verify that the given address is correct.</h4>';
        } 
        $res = $xml->response->results->result;
        $zestimate = $res->zestimate;
        $rentzestimate = $res->rentzestimate;
        $address = $res->address->street.', '
            .$res->address->city.', '
            .$res->address->state.'-'
            .$res->address->zipcode;
        $link = $res->links->homedetails;
        $addr_link = '<a href="'.$link.'" target="_blank">'.$address.'</a>';

setlocale(LC_MONETARY, 'en_US');
        $html = '<h2>Search Results</h2>';
        $html .= '<table>
                <tr class="tb-h"><td colspan="4">See more details for '.$addr_link.' on Zillow</td></tr><tr><td></td></tr>
                <tr>
                    <td>Property Type:</td>
                    <td>'.$res->useCode.'</td>
                    <td>Last Sold Price:</td>
                    <td>'.$this->moneyFormat($res->lastSoldPrice).'</td>
                </tr>
                <tr>
                    <td>Year Built:</td>
                    <td>'.$res->yearBuilt.'</td>
                    <td>Last Sold Date:</td>
                    <td>'.$this->_date($res->lastSoldDate).'</td>
                </tr>
                <tr>
                    <td>Lot Size:</td>
                    <td>'.$this->areaFormat($res->lotSizeSqFt).'</td>
                    <td>Zestimate ® Property Estimate as of '.$this->_date($zestimate->{'last-updated'}).':</td>
                    <td>'.$this->moneyFormat($zestimate->amount).'</td>
                </tr>
                <tr>
                    <td>Finished Area:</td>
                    <td>'.$this->areaFormat($res->finishedSqFt).'</td>
                    <td>30 Days Overall Change '.$this->change30Format($zestimate->valueChange,true).':</td>
                    <td>'.$this->change30Format($zestimate->valueChange).'</td>
                </tr>
                <tr>
                    <td>Bathrooms:</td>
                    <td>'.$res->bathrooms.'</td>
                    <td>All Time Property Range:</td>
                    <td>'.$this->rangeFormat($zestimate->valuationRange->low,$zestimate->valuationRange->high).'</td>
                </tr>
                <tr>
                    <td>Bedrooms:</td>
                    <td>'.$res->bedrooms.'</td>
                    <td>Rent Zestimate ® Rent Valuation as of '.$this->_date($rentzestimate->{'last-updated'}).':</td>
                    <td>'.$this->moneyFormat($rentzestimate->amount).'</td>
                </tr>
                <tr>
                    <td>Tax Assessment Year:</td>
                    <td>'.$res->taxAssessmentYear.'</td>
                    <td>30 Days Rent Change '.$this->change30Format($rentzestimate->valueChange,true).':</td>
                    <td>'.$this->change30Format($rentzestimate->valueChange).'</td>
                </tr>
                <tr>
                    <td>Tax Assessment:</td>
                    <td>'.$this->moneyFormat($res->taxAssessment).'</td>
                    <td>All Time Rent Range:</td>
                    <td>'.$this->rangeFormat($rentzestimate->valuationRange->low,$rentzestimate->valuationRange->high).'</td>
                </tr>
            </table>';    
        $html .= '<p>
                © Zillow, Inc., 2006-2014. Use is subject to 
                <a href="http://www.zillow.com/corp/Terms.htm">Terms of Use</a>
                <br>
                <a href="http://www.zillow.com/wikipages/What-is-a-Zestimate/">What\'s a Zestimate?</a>
            </p>';
        return $html;    
    }

    function moneyFormat($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return "$".number_format((double)$str,2,'.',',');
    }
    function areaFormat($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return number_format((double)$str)." sq.ft.";
    }
    function _date($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return date('d-M-Y',strtotime($str));
    }
    function rangeFormat($low,$high){
        $low = trim($low);
        $high = trim($high);
        if(empty($low) && empty($high)){
            return '';
        }
        return $this->moneyFormat($low).' - '.$this->moneyFormat($high); 
    }
    function change30Format($str,$imgf=0){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        $num = (double)$str;
        if($imgf){
            if($num <0){
                return '<img src="http://www-scf.usc.edu/~csci571/2014Spring/hw6/down_r.gif" />';
            }elseif($num > 0){
                return '<img src="http://www-scf.usc.edu/~csci571/2014Spring/hw6/up_g.gif" />';
            }else{
                return '';
            }
        }else{
            return $this->moneyFormat(abs($num));
        }
    }

    function view($xml){

        echo <<<HTML
<!Doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta author="Huaying Tsai">
    <title>CSCI571 HW6</title>
    <style>
        .container {
            text-align: center;
            width:960px;
            margin:0 auto;
        }
        form {
            width:450px;
            margin:0 auto;
        }
        fieldset{
            border: 2px solid black;
            text-align:left;
        }
        fieldset div {
            padding:2px;
        }
        fieldset div:first-child {
            padding-top:8px;
        }
        fieldset p{
            margin:10px 0 0 0;
            padding:0;
            font-style:italic;
            
        }
        fieldset label {
            float:left;
            width:120px;
        }
        .input{
            width:130px;
        }

        #submit{
            margin-left:120px;
            float:left;
        }
    
        table {
            margin:0 auto;    
        }

        table td{
            padding:3px 9px;
            text-align:left;
        }
        
        table .tb-h td{
            border: 1px solid black;
            border-radius:2px;
            background: #FEDB97;
            padding:1px;
            text-align:left !important;
        
        }
        table tr td:last-child{
            text-align:right;
        }
        table tr td:first-child{
            width:200px;
        }
        table a{
            font-weight:bold;
        }
    </style>
</head>    
<body>
    <div class="container">
    <h2>Real Estate Search</h2>
    <form>
        <fieldset>
            <div>
                <label>Street Address*:</label>
            <input class="input" type="text" name="street" value="{$this->input_data['street']}" />
            </div>
            <div>
                <label>City*:</label>
                <input class="input" type="text" name="city" value="{$this->input_data['city']}" />
            </div>
            <div>
                <label>State*:</label>
                    <select name="state" size="1">
                        <option value=""></option>
                        <option value="AK">AK</option>
                        <option value="AL">AL</option>
                        <option value="AR">AR</option>
                        <option value="AZ">AZ</option>
                        <option value="CA">CA</option>
                        <option value="CO">CO</option>
                        <option value="CT">CT</option>
                        <option value="DC">DC</option>
                        <option value="DE">DE</option>
                        <option value="FL">FL</option>
                        <option value="GA">GA</option>
                        <option value="HI">HI</option>
                        <option value="IA">IA</option>
                        <option value="ID">ID</option>
                        <option value="IL">IL</option>
                        <option value="IN">IN</option>
                        <option value="KS">KS</option>
                        <option value="KY">KY</option>
                        <option value="LA">LA</option>
                        <option value="MA">MA</option>
                        <option value="MD">MD</option>
                        <option value="ME">ME</option>
                        <option value="MI">MI</option>
                        <option value="MN">MN</option>
                        <option value="MO">MO</option>
                        <option value="MS">MS</option>
                        <option value="MT">MT</option>
                        <option value="NC">NC</option>
                        <option value="ND">ND</option>
                        <option value="NE">NE</option>
                        <option value="NH">NH</option>
                        <option value="NJ">NJ</option>
                        <option value="NM">NM</option>
                        <option value="NV">NV</option>
                        <option value="NY">NY</option>
                        <option value="OH">OH</option>
                        <option value="OK">OK</option>
                        <option value="OR">OR</option>
                        <option value="PA">PA</option>
                        <option value="RI">RI</option>
                        <option value="SC">SC</option>
                        <option value="SD">SD</option>
                        <option value="TN">TN</option>
                        <option value="TX">TX</option>
                        <option value="UT">UT</option>
                        <option value="VA">VA</option>
                        <option value="VT">VT</option>
                        <option value="WA">WA</option>
                        <option value="WI">WI</option>
                        <option value="WV">WV</option>
                        <option value="WY">WY</option>
                    </select>
            </div>
            <div>
                <input id="submit" type="submit" value="search" onclick="return submitCheck()"/>
                <img src="http://www.zillow.com/widgets/GetVersionedResource.htm?path=/static/logos/Zillowlogo_150x40_rounded.gif" width="150" height="40" alt="Zillow Real Estate Search" />
            </div>
            <p>* - Mandatory fields.</p>
        </fieldset>
    </form>
    {$this->resultHTML($xml)}
    
    </div>
    <script>
        var form = document.forms[0];

        form.state.value = "{$this->input_data['state']}";
        
        function submitCheck(){
            var ch = [], one = true,
                err_msg = "Please enter value for ";

            if(form.street.value.trim() === '') ch.push('Street Address');
            if(form.city.value.trim() === '') ch.push('City');
            if(form.state.value.trim() === '') ch.push('State');
            
            if(ch.length){ 
                for(id in ch){
                    if(one){
                        err_msg += ch[id];
                        one = false;
                    }else{
                        err_msg += " and "+ch[id]
                    }
                }
                err_msg += ".";
                alert(err_msg);
                return false;
            }
        }
    </script> 
</body>
</html>
HTML;
    
    }
}


?>
