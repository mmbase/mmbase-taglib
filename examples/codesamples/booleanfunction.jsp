<%!
public static boolean booleanFunction() {
    return System.currentTimeMillis() % 2 == 0;
}
%>
<mm:booleanfunction classname="THISPAGE" name="booleanFunction" id="yes">
  YES
</mm:booleanfunction>
<mm:booleanfunction referid="yes" inverse="true">
  NO
</mm:booleanfunction>